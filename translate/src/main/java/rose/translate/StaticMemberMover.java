package rose.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Static members a class lost to a holder class with the same member names ({@code EntityType.PIG} is
 * {@code EntityTypes.PIG} in 26.x). Runs after renaming: a static access the old owner no longer declares, which the
 * new owner does, is pointed at the new owner. Rules ({@code static-moves.tsv}): old owner, new owner, evidence.
 */
public final class StaticMemberMover implements ModTranslator.ExtraTransform {
    private final Map<String, String> moves;
    private final ClassIndex game;

    public StaticMemberMover(Map<String, String> moves, ClassIndex game) {
        this.moves = Map.copyOf(moves);
        this.game = game;
    }

    public static Map<String, String> read(Path file) throws IOException {
        Map<String, String> out = new HashMap<>();
        if (!Files.exists(file)) return out;
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 3 || p[2].isBlank()) throw new IOException(file + ": needs old<TAB>new<TAB>evidence: " + line);
            out.put(p[0], p[1]);
        }
        return out;
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String field, String fieldDesc) {
                        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) owner = moved(owner, field + ":" + fieldDesc, false);
                        super.visitFieldInsn(opcode, owner, field, fieldDesc);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String method, String methodDesc, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC) owner = moved(owner, method + methodDesc, true);
                        super.visitMethodInsn(opcode, owner, method, methodDesc, isInterface);
                    }
                };
            }
        };
    }

    String moved(String owner, String member, boolean method) {
        String target = moves.get(owner);
        if (target == null) return owner;
        ClassIndex.Info from = game.get(owner);
        ClassIndex.Info to = game.get(target);
        if (from == null || to == null) return owner;
        boolean declaredHere = method ? from.methods().contains(member) : from.fields().contains(member);
        boolean declaredThere = method ? to.methods().contains(member) : to.fields().contains(member);
        return !declaredHere && declaredThere ? target : owner;
    }
}
