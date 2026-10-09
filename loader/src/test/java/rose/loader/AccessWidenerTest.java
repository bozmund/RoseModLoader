package rose.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AccessWidenerTest {
    /** {@code public final class game/Sealed { private final Object values; private Sealed(Object) } } */
    private static byte[] sealed() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "game/Sealed", null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "values", "Ljava/lang/Object;", null, null).visitEnd();
        cw.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "(Ljava/lang/Object;)V", null, null).visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static Map<String, Integer> access(byte[] bytes) {
        Map<String, Integer> out = new HashMap<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                out.put("class", access);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                out.put(name, access);
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                out.put(name, access);
                return null;
            }
        }, 0);
        return out;
    }

    @Test
    void reopensFinalClassesAndPrivateMembers() {
        AccessWidener widener = new AccessWidener();
        widener.read("""
                accessWidener v2 named
                # comments and blank lines are fine

                extendable class game/Sealed
                accessible method game/Sealed <init> (Ljava/lang/Object;)V
                accessible field game/Sealed values Ljava/lang/Object;
                """, "test");
        Map<String, Integer> access = access(widener.transform("game/Sealed", sealed()));
        assertEquals(Opcodes.ACC_PUBLIC, access.get("class"), "public and no longer final");
        assertEquals(Opcodes.ACC_PUBLIC, access.get("<init>"));
        assertEquals(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, access.get("values"), "accessible keeps final; mutable removes it");
    }

    @Test
    void otherClassesAreUntouched() {
        AccessWidener widener = new AccessWidener();
        widener.read("accessWidener v2 named\nextendable class game/Other\n", "test");
        byte[] bytes = sealed();
        assertEquals(bytes, widener.transform("game/Sealed", bytes));
    }

    @Test
    void rejectsMalformedFiles() {
        assertThrows(IllegalArgumentException.class, () -> new AccessWidener().read("extendable class a/B\n", "no-header"));
        assertThrows(IllegalArgumentException.class, () -> new AccessWidener().read("accessWidener v2 named\nwide class a/B\n", "bad-access"));
    }
}
