package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CallAdapterTest {
    /** game/State implements game/Typed, whose generic is(T) erases to is(Object); game/Block extends Object. */
    private static CallAdapter adapter(Path dir) throws IOException {
        Path jar = dir.resolve("game.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (var e : Map.of(
                    "game/Typed", type("game/Typed", "java/lang/Object", true,
                            new String[] {"is", "(Ljava/lang/Object;)Z"}, new String[] {"value", "()Ljava/lang/Object;", "()TT;"}),
                    "game/State", type("game/State", "java/lang/Object", false,
                            new String[] {"setBlock", "(Lgame/Block;)Lgame/State;"}),
                    "game/Block", type("game/Block", "java/lang/Object", false),
                    "game/Overloaded", type("game/Overloaded", "java/lang/Object", false,
                            new String[] {"put", "(Ljava/lang/Object;)V"}, new String[] {"put", "(Ljava/lang/CharSequence;)V"})).entrySet()) {
                out.putNextEntry(new ZipEntry(e.getKey() + ".class"));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        ClassIndex game = ClassIndex.of(java.util.List.of(jar), true);
        return new CallAdapter(new ClassIndex(false), game);
    }

    private static byte[] type(String name, String superName, boolean isInterface, String[]... methods) {
        ClassWriter cw = new ClassWriter(0);
        String[] interfaces = name.equals("game/State") ? new String[] {"game/Typed"} : null;
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | (isInterface ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT : 0),
                name, null, superName, interfaces);
        for (String[] m : methods) {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, m[0], m[1], m.length > 2 ? m[2] : null, null);
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Test
    void genericParameterIsAdaptedWithoutCast(@TempDir Path dir) throws IOException {
        CallAdapter.Adaptation a = adapter(dir).find("game/State", "is", "(Lgame/Block;)Z", false);
        assertEquals(new CallAdapter.Adaptation("(Ljava/lang/Object;)Z", null, false), a);
    }

    @Test
    void widerReturnTypeGetsACast(@TempDir Path dir) throws IOException {
        CallAdapter.Adaptation a = adapter(dir).find("game/State", "value", "()Lgame/Block;", false);
        assertEquals(new CallAdapter.Adaptation("()Ljava/lang/Object;", "game/Block", false), a);
    }

    @Test
    void newReturnValueIsPoppedWhenTheOldCallWasVoid(@TempDir Path dir) throws IOException {
        CallAdapter.Adaptation a = adapter(dir).find("game/State", "setBlock", "(Lgame/Block;)V", false);
        assertEquals(new CallAdapter.Adaptation("(Lgame/Block;)Lgame/State;", null, true), a);
    }

    @Test
    void ambiguousOrIncompatibleCallsAreNotAdapted(@TempDir Path dir) throws IOException {
        CallAdapter adapter = adapter(dir);
        assertNull(adapter.find("game/Overloaded", "put", "(Ljava/lang/String;)V", false), "two applicable overloads");
        assertNull(adapter.find("game/State", "is", "(I)Z", false), "primitive can't widen to Object");
        assertNull(adapter.find("game/State", "is", "(Lgame/Block;)Z", true), "static-ness must match");
    }

    @Test
    void returnTypesThatChangedMeaningAreNotAdapted(@TempDir Path dir) throws IOException {
        CallAdapter adapter = adapter(dir);
        // Old code expected the value (Object, cast by the caller); a different returned object would fail at runtime.
        assertNull(adapter.find("game/State", "setBlock", "(Lgame/Block;)Ljava/lang/Object;", false));
        // A wider return type without a type variable in the signature is a real API change, not erasure.
        assertNull(adapter.find("game/State", "setBlock", "(Lgame/Block;)Lgame/Block;", false));
    }

    @Test
    void holderConversionsAreUsedForArgumentsReturnsAndGenericChecks(@TempDir Path dir) throws IOException {
        Path jar = dir.resolve("holders.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (var e : Map.of(
                    "game/Holder", type("game/Holder", "java/lang/Object", true),
                    "game/Effect", type("game/Effect", "java/lang/Object", false),
                    "game/Attribute", type("game/Attribute", "java/lang/Object", false),
                    "game/Instance", type("game/Instance", "java/lang/Object", false,
                            new String[] {"apply", "(Lgame/Holder;I)V", "(Lgame/Holder<Lgame/Effect;>;I)V"},
                            new String[] {"getEffect", "()Lgame/Holder;"})).entrySet()) {
                out.putNextEntry(new ZipEntry(e.getKey() + ".class"));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        var conversions = new rose.rosetta.ConversionRules(Map.of(
                "game/Effect->game/Holder", new rose.rosetta.ConversionRules.Conversion("game/Effect", "game/Holder", "h/H", "wrap", "test"),
                "game/Holder->*", new rose.rosetta.ConversionRules.Conversion("game/Holder", "*", "h/H", "value", "test")));
        CallAdapter adapter = new CallAdapter(new ClassIndex(false), ClassIndex.of(java.util.List.of(jar), true), conversions);

        CallAdapter.Adaptation apply = adapter.find("game/Instance", "apply", "(Lgame/Effect;I)V", false);
        assertEquals("(Lgame/Holder;I)V", apply.newDesc());
        assertEquals("wrap", apply.argConversions().get(0).helperName());
        assertNull(apply.argConversions().get(1));

        CallAdapter.Adaptation get = adapter.find("game/Instance", "getEffect", "()Lgame/Effect;", false);
        assertEquals("value", get.returnConversion().helperName());
        assertEquals("game/Effect", get.castTo(), "Holder.value() is cast back to the old type");

        assertNull(adapter.find("game/Instance", "apply", "(Lgame/Attribute;I)V", false), "Holder<Effect> doesn't take an Attribute");
    }

    @Test
    void genericParameterTypeArgumentsAreRead() {
        assertEquals(java.util.Arrays.asList("game/Effect", null, "T"),
                CallAdapter.paramTypeArguments("<T:Ljava/lang/Object;>(Lgame/Holder<Lgame/Effect;>;ILgame/Holder<TT;>;)V", 3));
    }
}
