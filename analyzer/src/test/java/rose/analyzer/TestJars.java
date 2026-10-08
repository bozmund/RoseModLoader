package rose.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Builds tiny jars of synthetic classes for analyzer tests (no Minecraft needed). */
final class TestJars {
    /** A class with the given super class and abstract/empty methods {@code name+desc}. */
    static byte[] type(String name, String superName, String... methods) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, superName, null);
        for (String m : methods) {
            int paren = m.indexOf('(');
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m.substring(0, paren), m.substring(paren), null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** A class whose method {@code run()} calls {@code owner.name desc} on a fresh local (null) receiver. */
    static byte[] caller(String name, String owner, String method, String desc) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, method, desc, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    static Path jar(Path dir, String fileName, Map<String, byte[]> classes) throws IOException {
        Path jar = dir.resolve(fileName);
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (var e : classes.entrySet()) {
                out.putNextEntry(new ZipEntry(e.getKey() + ".class"));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        return jar;
    }

    private TestJars() {}
}
