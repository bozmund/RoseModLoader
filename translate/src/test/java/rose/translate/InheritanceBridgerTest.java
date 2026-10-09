package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import rose.rosetta.BridgeRules;
import rose.rosetta.BridgeRules.Bridge;

class InheritanceBridgerTest {
    /** game/Api { abstract int newMethod(); abstract String group(); } ; abstract class game/Base implements game/Api */
    private static Map<String, byte[]> game() {
        return Map.of(
                "game/Api", type("game/Api", "java/lang/Object", Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, new String[0],
                        new String[] {"newMethod", "()I", "abstract"}, new String[] {"group", "()Ljava/lang/String;", "abstract"}),
                "game/Base", type("game/Base", "java/lang/Object", Opcodes.ACC_ABSTRACT, new String[] {"game/Api"}));
    }

    /** mod/Impl extends game/Base, declares oldMethod(); mod/Sub extends mod/Impl. */
    private static Map<String, byte[]> mod() {
        return Map.of(
                "mod/Impl", type("mod/Impl", "game/Base", 0, new String[0], new String[] {"oldMethod", "()I", "concrete"}),
                "mod/Sub", type("mod/Sub", "mod/Impl", 0, new String[0]));
    }

    private static byte[] type(String name, String superName, int access, String[] interfaces, String[]... methods) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | access, name, null, superName, interfaces);
        for (String[] m : methods) {
            boolean isAbstract = m[2].equals("abstract");
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | (isAbstract ? Opcodes.ACC_ABSTRACT : 0), m[0], m[1], null, null);
            if (!isAbstract) {
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitMaxs(0, 0);
            }
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static ClassIndex index(Path dir, String file, Map<String, byte[]> classes) throws IOException {
        Path jar = dir.resolve(file);
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (var e : classes.entrySet()) {
                out.putNextEntry(new ZipEntry(e.getKey() + ".class"));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        return ClassIndex.of(List.of(jar), true);
    }

    private static final BridgeRules RULES = new BridgeRules(List.of(
            new Bridge("game/Api", "newMethod()I", "oldMethod()I", "shim/Bridges", "newMethod", "test"),
            new Bridge("game/Api", "group()Ljava/lang/String;", "-", "shim/Bridges", "group", "test")));

    @Test
    void addsBridgesWhereTheModHasTheOldMethodOrLacksARequiredOne(@TempDir Path dir) throws IOException {
        InheritanceBridger bridger = new InheritanceBridger(RULES, index(dir, "mod.jar", mod()), index(dir, "game.jar", game()));
        assertEquals(List.of("newMethod()I", "group()Ljava/lang/String;"),
                bridger.bridgesFor("mod/Impl").stream().map(Bridge::method).toList());
        assertTrue(bridger.bridgesFor("mod/Sub").isEmpty(), "the subclass inherits both bridges from mod/Impl");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(mod().get("mod/Impl")).accept(bridger.wrap(writer), 0);
        ClassNode node = new ClassNode();
        new ClassReader(writer.toByteArray()).accept(node, 0);
        MethodNode bridge = node.methods.stream().filter(m -> m.name.equals("newMethod")).findFirst().orElseThrow();
        MethodInsnNode call = (MethodInsnNode) java.util.Arrays.stream(bridge.instructions.toArray())
                .filter(i -> i instanceof MethodInsnNode).findFirst().orElseThrow();
        assertEquals("shim/Bridges.newMethod(Lgame/Api;)I", call.owner + "." + call.name + call.desc);
    }
}
