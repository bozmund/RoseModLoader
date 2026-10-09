package rose.loader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Loosens access to game classes and members, so mods (and dialects re-creating old APIs) can extend classes that
 * became final or call constructors that became private. Uses Fabric's access widener format (named namespace):
 *
 * <pre>
 * accessWidener v2 named
 * extendable class net/minecraft/world/item/crafting/Ingredient
 * accessible method net/minecraft/world/item/crafting/Ingredient &lt;init&gt; (Lnet/minecraft/core/HolderSet;)V
 * accessible field  net/minecraft/world/entity/animal/Chicken FOOD_ITEMS Lnet/minecraft/world/item/crafting/Ingredient;
 * mutable field     ...   (removes final)
 * </pre>
 *
 * {@code extendable} removes {@code final}; {@code accessible} makes public (a method also loses {@code final} if
 * it was private, as Fabric does).
 */
public final class AccessWidener implements ClassTransformer {
    private final Set<String> extendableClasses = new HashSet<>();
    private final Set<String> accessibleClasses = new HashSet<>();
    private final Map<String, Set<String>> accessibleMembers = new HashMap<>(); // owner -> name+desc
    private final Map<String, Set<String>> extendableMethods = new HashMap<>();
    private final Map<String, Set<String>> mutableFields = new HashMap<>();

    /** Adds the entries of one access widener file. {@code source} names it in error messages. */
    public void read(String text, String source) {
        String[] lines = text.split("\\R");
        boolean header = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("#.*", "").strip();
            if (line.isEmpty()) continue;
            String[] p = line.split("\\s+");
            if (!header) {
                if (p.length != 3 || !p[0].equals("accessWidener") || !p[2].equals("named")) {
                    throw new IllegalArgumentException(source + ":" + (i + 1) + ": expected 'accessWidener v1|v2 named'");
                }
                header = true;
                continue;
            }
            String access = p[0].startsWith("transitive-") ? p[0].substring("transitive-".length()) : p[0];
            if (p.length == 3 && p[1].equals("class")) {
                switch (access) {
                    case "extendable" -> extendableClasses.add(p[2]);
                    case "accessible" -> accessibleClasses.add(p[2]);
                    default -> throw bad(source, i, line);
                }
            } else if (p.length == 5 && (p[1].equals("method") || p[1].equals("field"))) {
                String key = p[3] + p[4];
                switch (access) {
                    case "accessible" -> accessibleMembers.computeIfAbsent(p[2], k -> new HashSet<>()).add(key);
                    case "extendable" -> extendableMethods.computeIfAbsent(p[2], k -> new HashSet<>()).add(key);
                    case "mutable" -> mutableFields.computeIfAbsent(p[2], k -> new HashSet<>()).add(key);
                    default -> throw bad(source, i, line);
                }
                // Widening a member also makes its class reachable.
                if (!access.equals("mutable")) accessibleClasses.add(p[2]);
                if (access.equals("extendable")) extendableClasses.add(p[2]);
            } else {
                throw bad(source, i, line);
            }
        }
    }

    private static IllegalArgumentException bad(String source, int line, String text) {
        return new IllegalArgumentException(source + ":" + (line + 1) + ": can't parse '" + text + "'");
    }

    public boolean isEmpty() {
        return extendableClasses.isEmpty() && accessibleClasses.isEmpty() && accessibleMembers.isEmpty()
                && extendableMethods.isEmpty() && mutableFields.isEmpty();
    }

    @Override
    public byte[] transform(String internalName, byte[] bytes) {
        if (!extendableClasses.contains(internalName) && !accessibleClasses.contains(internalName)) return bytes;
        Set<String> accessible = accessibleMembers.getOrDefault(internalName, Set.of());
        Set<String> extendable = extendableMethods.getOrDefault(internalName, Set.of());
        Set<String> mutable = mutableFields.getOrDefault(internalName, Set.of());
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, classAccess(name, access), name, signature, superName, interfaces);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                super.visitInnerClass(name, outerName, innerName, classAccess(name, access));
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                String key = name + desc;
                if (accessible.contains(key)) access = makePublic(access);
                if (mutable.contains(key)) access &= ~Opcodes.ACC_FINAL;
                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                String key = name + desc;
                if (accessible.contains(key)) {
                    if ((access & Opcodes.ACC_PRIVATE) != 0 && !name.equals("<init>")) access |= Opcodes.ACC_FINAL;
                    access = makePublic(access);
                }
                if (extendable.contains(key)) {
                    access &= ~Opcodes.ACC_FINAL;
                    if ((access & Opcodes.ACC_PRIVATE) != 0) access = (access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PROTECTED;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);
        return writer.toByteArray();
    }

    private int classAccess(String name, int access) {
        if (extendableClasses.contains(name)) {
            access &= ~Opcodes.ACC_FINAL;
            access = makePublic(access);
        } else if (accessibleClasses.contains(name)) {
            access = makePublic(access);
        }
        return access;
    }

    private static int makePublic(int access) {
        return (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
    }
}
