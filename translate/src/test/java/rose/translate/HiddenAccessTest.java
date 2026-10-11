package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class HiddenAccessTest {
    @TempDir
    Path tmp;

    @Test
    void privateGameMembersTheModUsesBecomeAccessible() throws IOException {
        HiddenAccess hidden = new HiddenAccess(game());
        hidden.scan(mod("mod/Tabs"));
        assertEquals(Set.of(
                "accessible field net/minecraft/world/item/CreativeModeTabs SPAWN_EGGS Ljava/lang/Object;",
                "accessible method net/minecraft/world/item/CreativeModeTabs bootstrap ()V"), hidden.entries());
    }

    @Test
    void finalGameFieldsTheModAssignsBecomeMutable() throws IOException {
        Path jar = tmp.resolve("screens.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            ClassWriter cw = new ClassWriter(0);
            cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "net/minecraft/client/gui/screens/Screen", null, "java/lang/Object", null);
            cw.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, "imageWidth", "I", null, null).visitEnd();
            cw.visitEnd();
            out.putNextEntry(new ZipEntry("net/minecraft/client/gui/screens/Screen.class"));
            out.write(cw.toByteArray());
            out.closeEntry();
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/ChestScreen", null, "net/minecraft/client/gui/screens/Screen", null);
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC, "init", "()V", null, null);
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitIntInsn(Opcodes.SIPUSH, 230);
        m.visitFieldInsn(Opcodes.PUTFIELD, "mod/ChestScreen", "imageWidth", "I");
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();
        Path modJar = tmp.resolve("mod.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(modJar))) {
            out.putNextEntry(new ZipEntry("mod/ChestScreen.class"));
            out.write(cw.toByteArray());
            out.closeEntry();
        }
        // javac addresses the inherited field through the mod's own class; the mod index leads to the game class
        HiddenAccess hidden = new HiddenAccess(ClassIndex.of(List.of(jar), false), ClassIndex.of(List.of(modJar), false));
        hidden.scan(cw.toByteArray());
        assertEquals(Set.of("mutable field net/minecraft/client/gui/screens/Screen imageWidth I"), hidden.entries());
    }

    @Test
    void samePackageAndPublicMembersNeedNothing() throws IOException {
        HiddenAccess hidden = new HiddenAccess(game());
        hidden.scan(mod("net/minecraft/world/item/ModTabs"));
        assertEquals(Set.of(), hidden.entries());
    }

    @Test
    void entriesExtendTheModsOwnWidener() {
        Set<String> hidden = new java.util.TreeSet<>(Set.of("accessible field a/B c I", "accessible class a/C"));
        String own = "accessWidener v2 named\naccessible class a/C\n";
        List<ModTranslator.Resource> out = ModTranslator.withHiddenAccess(
                List.of(new ModTranslator.Resource(ModTranslator.ACCESS_WIDENER, own.getBytes(StandardCharsets.UTF_8))), hidden);
        assertEquals("accessWidener v2 named\naccessible class a/C\naccessible field a/B c I\n",
                new String(out.getFirst().content(), StandardCharsets.UTF_8));
        out = ModTranslator.withHiddenAccess(List.of(), hidden);
        assertEquals("accessWidener v2 named\naccessible class a/C\naccessible field a/B c I\n",
                new String(out.getFirst().content(), StandardCharsets.UTF_8));
    }

    private ClassIndex game() throws IOException {
        Path jar = tmp.resolve("game.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            ClassWriter cw = new ClassWriter(0);
            cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "net/minecraft/world/item/CreativeModeTabs", null, "java/lang/Object", null);
            cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "SPAWN_EGGS", "Ljava/lang/Object;", null, null).visitEnd();
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "COMBAT", "Ljava/lang/Object;", null, null).visitEnd();
            cw.visitMethod(Opcodes.ACC_STATIC, "bootstrap", "()V", null, null).visitEnd();
            cw.visitEnd();
            out.putNextEntry(new ZipEntry("net/minecraft/world/item/CreativeModeTabs.class"));
            out.write(cw.toByteArray());
            out.closeEntry();
        }
        return ClassIndex.of(List.of(jar), false);
    }

    private static byte[] mod(String name) {
        String tabs = "net/minecraft/world/item/CreativeModeTabs";
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
        m.visitCode();
        m.visitFieldInsn(Opcodes.GETSTATIC, tabs, "SPAWN_EGGS", "Ljava/lang/Object;");
        m.visitInsn(Opcodes.POP);
        m.visitFieldInsn(Opcodes.GETSTATIC, tabs, "COMBAT", "Ljava/lang/Object;");
        m.visitInsn(Opcodes.POP);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, tabs, "bootstrap", "()V", false);
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
