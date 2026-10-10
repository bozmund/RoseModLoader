package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import rose.rosetta.MixinRetargetRules;
import rose.rosetta.MixinRetargetRules.Retarget;

class MixinRebaserTest {
    private static final String TARGET = "game/Dispenser";
    private static final String MIXIN = "mod/DispenserMixin";
    private static final String CI = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String OLD = "L" + TARGET + ";dispense(Lgame/Level;Lgame/Pos;)V";
    private static final String NEW = "L" + TARGET + ";dispense(Lgame/ServerLevel;Lgame/State;Lgame/Pos;)V";

    @TempDir
    Path tmp;

    @Test
    void injectHandlerMovesToTheNewSignatureAndCapturedLocalsBecomeLocals() throws IOException {
        MixinRebaser rebaser = rebaser(false);
        String refmap = rebaser.retargetRefmap("{\"mappings\":{\"" + MIXIN + "\":{\"dispense\":\"" + OLD + "\"}}}");
        assertTrue(refmap.contains(NEW.replace("/", "/")), refmap);

        String oldHandler = "(Lgame/Level;Lgame/Pos;" + CI + "Lgame/Source;I)V";
        ClassNode out = read(rebaser.rebase(mixin(oldHandler, true, false)));

        MethodNode adapter = method(out, "onDispense$rose");
        assertNotNull(adapter);
        assertEquals("(Lgame/ServerLevel;Lgame/State;Lgame/Pos;" + CI + "Lgame/Source;I)V", adapter.desc);
        AnnotationNode inject = adapter.visibleAnnotations.getFirst();
        assertEquals("Lorg/spongepowered/asm/mixin/injection/Inject;", inject.desc);
        assertFalse(inject.values.contains("locals"), "local capture is replaced by @Local");
        assertEquals("Lcom/llamalad7/mixinextras/sugar/Local;", adapter.invisibleParameterAnnotations[4].getFirst().desc);
        assertEquals("Lcom/llamalad7/mixinextras/sugar/Local;", adapter.invisibleParameterAnnotations[5].getFirst().desc);
        assertNull(adapter.invisibleParameterAnnotations[0]);

        MethodNode old = method(out, "onDispense");
        assertEquals(oldHandler, old.desc, "the old handler keeps its body and descriptor");
        assertTrue((old.access & Opcodes.ACC_PRIVATE) != 0);
        assertTrue(old.visibleAnnotations == null || old.visibleAnnotations.isEmpty());
    }

    @Test
    void anArgumentWithNoCounterpartIsOnlyDroppedWhenUnread() throws IOException {
        MixinRebaser rebaser = rebaser(false);
        rebaser.retargetRefmap("{\"mappings\":{\"" + MIXIN + "\":{\"dispense\":\"" + OLD + "\"}}}");
        // reads its Level (no 26.3 counterpart: ServerLevel isn't one here) -> left for Mixin to report
        ClassNode out = read(rebaser.rebase(mixin("(Lgame/Level;Lgame/Pos;" + CI + ")V", false, true)));
        assertNull(method(out, "onDispense$rose"));
        assertTrue(rebaser.report().stream().anyMatch(r -> r.contains("which it reads")), rebaser.report().toString());
    }

    @Test
    void subtypesStandInForOldArgumentTypes() throws IOException {
        MixinRebaser rebaser = rebaser(true); // game/ServerLevel extends game/Level
        rebaser.retargetRefmap("{\"mappings\":{\"" + MIXIN + "\":{\"dispense\":\"" + OLD + "\"}}}");
        ClassNode out = read(rebaser.rebase(mixin("(Lgame/Level;Lgame/Pos;" + CI + ")V", false, true)));
        MethodNode adapter = method(out, "onDispense$rose");
        assertNotNull(adapter, rebaser.report().toString());
        assertEquals("(Lgame/ServerLevel;Lgame/State;Lgame/Pos;" + CI + ")V", adapter.desc);
    }

    @Test
    void unusedShadowsOfMovedMethodsAreDropped() throws IOException {
        MixinRebaser rebaser = rebaser(false);
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, MIXIN, null, "java/lang/Object", null);
        mixinAnnotation(cw);
        MethodVisitor shadow = cw.visitMethod(Opcodes.ACC_PROTECTED | Opcodes.ACC_ABSTRACT, "dispense", "(Lgame/Level;Lgame/Pos;)V", null, null);
        shadow.visitAnnotation("Lorg/spongepowered/asm/mixin/Shadow;", true).visitEnd();
        shadow.visitEnd();
        cw.visitEnd();
        ClassNode out = read(rebaser.rebase(cw.toByteArray()));
        assertNull(method(out, "dispense"));
    }

    @Test
    void rulesNeedEvidenceAndStayInTheirClass() throws IOException {
        Path file = tmp.resolve("rules.tsv");
        Files.writeString(file, OLD + "\t" + NEW + "\n");
        assertThrows(file);
        Files.writeString(file, OLD + "\tLother;dispense()V\tmoved\n");
        assertThrows(file);
        Files.writeString(file, OLD + "\t" + NEW + "\t1.20.2: dispense gained the state\n");
        assertEquals("dispense", MixinRetargetRules.read(file).find(OLD).newName());
    }

    private static void assertThrows(Path file) {
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> MixinRetargetRules.read(file));
    }

    private MixinRebaser rebaser(boolean serverLevelIsALevel) throws IOException {
        Path jar = tmp.resolve("game" + serverLevelIsALevel + ".jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            entry(out, TARGET, "java/lang/Object", writer -> {
                writer.visitMethod(Opcodes.ACC_PROTECTED, "dispense", "(Lgame/ServerLevel;Lgame/State;Lgame/Pos;)V", null, null).visitEnd();
            });
            entry(out, "game/Level", "java/lang/Object", writer -> { });
            entry(out, "game/ServerLevel", serverLevelIsALevel ? "game/Level" : "java/lang/Object", writer -> { });
        }
        ClassIndex game = ClassIndex.of(List.of(jar), false);
        return new MixinRebaser(new MixinRetargetRules(Map.of(OLD, new Retarget(OLD, NEW, "test"))), game);
    }

    private static void entry(ZipOutputStream out, String name, String superName, java.util.function.Consumer<ClassWriter> body) throws IOException {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, superName, null);
        body.accept(cw);
        cw.visitEnd();
        out.putNextEntry(new ZipEntry(name + ".class"));
        out.write(cw.toByteArray());
        out.closeEntry();
    }

    /** A Mixin with one @Inject handler into "dispense"; {@code readsFirst}: its body reads its first argument. */
    private static byte[] mixin(String handlerDesc, boolean capture, boolean readsFirst) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, MIXIN, null, "java/lang/Object", null);
        mixinAnnotation(cw);
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC, "onDispense", handlerDesc, null, null);
        AnnotationVisitor inject = m.visitAnnotation("Lorg/spongepowered/asm/mixin/injection/Inject;", true);
        AnnotationVisitor methods = inject.visitArray("method");
        methods.visit(null, "dispense");
        methods.visitEnd();
        if (capture) inject.visitEnum("locals", "Lorg/spongepowered/asm/mixin/injection/callback/LocalCapture;", "CAPTURE_FAILHARD");
        inject.visitEnd();
        m.visitCode();
        if (readsFirst) {
            m.visitVarInsn(Opcodes.ALOAD, 1);
            m.visitInsn(Opcodes.POP);
        }
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void mixinAnnotation(ClassWriter cw) {
        AnnotationVisitor mixin = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor value = mixin.visitArray("value");
        value.visit(null, Type.getObjectType(TARGET));
        value.visitEnd();
        mixin.visitEnd();
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null);
    }
}
