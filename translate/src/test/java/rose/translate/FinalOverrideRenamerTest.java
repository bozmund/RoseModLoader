package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class FinalOverrideRenamerTest {
    private static final String DESC = "(I)V";
    private static final FinalOverrideRenamer.Rule RULE =
            new FinalOverrideRenamer.Rule("game/Widget", "draw", DESC, "legacy$draw", "era/LegacyDraw");

    /** game/Widget { final void draw(int) } */
    private static Map<String, byte[]> game() {
        return Map.of("game/Widget", type("game/Widget", "java/lang/Object", null, false));
    }

    /**
     * mod/Button extends game/Widget and overrides draw, calling super.draw; mod/Fancy extends mod/Button and
     * overrides draw, calling super.draw (mod/Button's); mod/Plain extends game/Widget without draw; mod/Other is
     * no widget but has a draw(int).
     */
    private static Map<String, byte[]> mod() {
        return Map.of(
                "mod/Button", type("mod/Button", "game/Widget", "game/Widget", true),
                "mod/Fancy", type("mod/Fancy", "mod/Button", "mod/Button", true),
                "mod/Plain", type("mod/Plain", "game/Widget", null, false),
                "mod/Other", type("mod/Other", "java/lang/Object", null, true));
    }

    /** A class; with {@code draws}, a draw(int) that calls {@code superCall}.draw(int) when that isn't null. */
    private static byte[] type(String name, String superName, String superCall, boolean draws) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, superName, new String[0]);
        if (draws || name.startsWith("game/")) {
            int access = Opcodes.ACC_PUBLIC | (name.startsWith("game/") ? Opcodes.ACC_FINAL : 0);
            MethodVisitor mv = cw.visitMethod(access, "draw", DESC, null, null);
            mv.visitCode();
            if (superCall != null) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superCall, "draw", DESC, false);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
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

    private static ClassNode renamed(FinalOverrideRenamer renamer, byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        renamer.rename(node);
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null);
    }

    private static MethodInsnNode superCall(MethodNode method) {
        for (var insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL) return call;
        }
        return null;
    }

    @Test
    void overridesOfTheFinalMethodAreRenamedAndMarked(@TempDir Path dir) throws IOException {
        Map<String, byte[]> mod = mod();
        FinalOverrideRenamer renamer = new FinalOverrideRenamer(List.of(RULE), index(dir, "mod.jar", mod), index(dir, "game.jar", game()));

        ClassNode button = renamed(renamer, mod.get("mod/Button"));
        assertTrue(method(button, "draw") == null && method(button, "legacy$draw") != null, "the override is renamed");
        assertEquals(List.of("era/LegacyDraw"), button.interfaces);
        assertEquals("draw", superCall(method(button, "legacy$draw")).name, "super.draw on the game class still reaches the final method");

        ClassNode fancy = renamed(renamer, mod.get("mod/Fancy"));
        assertEquals("legacy$draw", superCall(method(fancy, "legacy$draw")).name, "super.draw on mod/Button reaches its renamed override");
    }

    @Test
    void classesWithoutTheOverrideOrOutsideTheTargetAreUntouched(@TempDir Path dir) throws IOException {
        Map<String, byte[]> mod = mod();
        FinalOverrideRenamer renamer = new FinalOverrideRenamer(List.of(RULE), index(dir, "mod.jar", mod), index(dir, "game.jar", game()));

        ClassNode plain = renamed(renamer, mod.get("mod/Plain"));
        assertTrue(plain.interfaces.isEmpty());
        ClassNode other = renamed(renamer, mod.get("mod/Other"));
        assertTrue(method(other, "draw") != null && other.interfaces.isEmpty(), "a draw(int) outside game/Widget keeps its name");
        assertFalse(other.interfaces.contains("era/LegacyDraw"));
    }

    @Test
    void readsRulesWithEvidence(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("final-overrides.tsv");
        Files.writeString(file, "# comment\ngame/Widget\tdraw(I)V\tlegacy$draw\tera/LegacyDraw\tevidence\n");
        assertEquals(List.of(RULE), FinalOverrideRenamer.read(file));
        Files.writeString(file, "game/Widget\tdraw(I)V\tlegacy$draw\tera/LegacyDraw\n");
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> FinalOverrideRenamer.read(file));
    }
}
