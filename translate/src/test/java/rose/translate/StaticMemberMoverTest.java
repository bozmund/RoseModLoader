package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class StaticMemberMoverTest {
    @TempDir
    Path tmp;

    @Test
    void onlyMembersTheOldOwnerLostMove() throws IOException {
        Path jar = tmp.resolve("game.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            entry(out, "game/EntityType", "CODEC");
            entry(out, "game/EntityTypes", "PIG");
        }
        StaticMemberMover mover = new StaticMemberMover(Map.of("game/EntityType", "game/EntityTypes"), ClassIndex.of(List.of(jar), false));
        assertEquals("game/EntityTypes", mover.moved("game/EntityType", "PIG:Lgame/EntityType;", false));
        assertEquals("game/EntityType", mover.moved("game/EntityType", "CODEC:Lgame/EntityType;", false), "still declared there");
        assertEquals("game/EntityType", mover.moved("game/EntityType", "GONE:Lgame/EntityType;", false), "declared nowhere");
        assertEquals("game/Other", mover.moved("game/Other", "PIG:Lgame/EntityType;", false));
    }

    private static void entry(ZipOutputStream out, String name, String field) throws IOException {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, field, "Lgame/EntityType;", null, null).visitEnd();
        cw.visitEnd();
        out.putNextEntry(new ZipEntry(name + ".class"));
        out.write(cw.toByteArray());
        out.closeEntry();
    }
}
