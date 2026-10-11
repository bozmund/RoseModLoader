package rose.translate;

import java.util.Set;
import java.util.TreeSet;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Game members a translated mod uses that 26.3 no longer lets it reach: fields, methods and classes that were public
 * or protected in the old version and are private or package-private now (1.20.1 mods read
 * {@code CreativeModeTabs.SPAWN_EGGS}; 26.3 made it private), and fields the mod assigns that 26.3 made final (old
 * screens set {@code AbstractContainerScreen.imageWidth}). They become access widener entries for the mod, so the old
 * code links as it did.
 */
public final class HiddenAccess {
    private final ClassIndex game;
    private final ClassIndex mod;
    private final Set<String> entries = new TreeSet<>();

    /** @param mod the mod's own classes, so members reached through them are traced to the game class declaring them */
    public HiddenAccess(ClassIndex game, ClassIndex mod) {
        this.game = game;
        this.mod = mod;
    }

    public HiddenAccess(ClassIndex game) {
        this(game, new ClassIndex(false));
    }

    /** Looks at one translated class. */
    public void scan(byte[] classBytes) {
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            private String self;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                self = name;
                if (superName != null) classRef(superName);
                if (interfaces != null) for (String i : interfaces) classRef(i);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String field, String fieldDesc) {
                        member(self, owner, field + ":" + fieldDesc, "field " + field + " " + fieldDesc, false);
                        if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) assigned(owner, field, fieldDesc);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String method, String methodDesc, boolean isInterface) {
                        if (owner.startsWith("[")) return;
                        member(self, owner, method + methodDesc, "method " + method + " " + methodDesc, true);
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (!type.startsWith("[")) classRef(type);
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    /** The access widener entries found so far ({@code accessible <kind> <owner> ...}), sorted. */
    public Set<String> entries() {
        return entries;
    }

    private void classRef(String type) {
        ClassIndex.Info info = game.get(type);
        if (info != null && (info.access() & Opcodes.ACC_PUBLIC) == 0 && isGame(type)) entries.add("accessible class " + type);
    }

    private void member(String from, String owner, String key, String entry, boolean method) {
        for (ClassIndex.Info c = info(owner); c != null; c = c.superName() == null ? null : info(c.superName())) {
            boolean declared = method ? c.methods().contains(key) : c.fields().contains(key);
            if (!declared) continue;
            if (isGame(c.name()) && c.hidden().contains(key) && !packageOf(c.name()).equals(packageOf(from))) {
                entries.add("accessible " + entry.replaceFirst(" ", " " + c.name() + " "));
                classRef(c.name());
            }
            return;
        }
    }

    private void assigned(String owner, String field, String desc) {
        String key = field + ":" + desc;
        for (ClassIndex.Info c = info(owner); c != null; c = c.superName() == null ? null : info(c.superName())) {
            if (!c.fields().contains(key)) continue;
            if (isGame(c.name()) && c.finalFields().contains(key)) entries.add("mutable field " + c.name() + " " + field + " " + desc);
            return;
        }
    }

    private ClassIndex.Info info(String type) {
        ClassIndex.Info own = mod.get(type);
        return own != null ? own : game.get(type);
    }

    private static boolean isGame(String type) {
        return type.startsWith("net/minecraft/") || type.startsWith("com/mojang/");
    }

    private static String packageOf(String type) {
        int slash = type.lastIndexOf('/');
        return slash < 0 ? "" : type.substring(0, slash);
    }
}
