package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodeHashTest {
    @TempDir
    Path tmp;

    @Test
    void changesWhenAClassChanges() throws IOException {
        Path dir = Files.createDirectories(tmp.resolve("classes/rose/translate"));
        Files.writeString(dir.resolve("A.class"), "one");
        String before = hash(tmp.resolve("classes"));
        Files.writeString(dir.resolve("A.class"), "two");
        assertNotEquals(before, hash(tmp.resolve("classes")));
    }

    @Test
    void sameForAJarAndItsDirectory() throws IOException {
        Path dir = Files.createDirectories(tmp.resolve("classes/rose"));
        Files.writeString(dir.resolve("A.class"), "a");
        Files.writeString(dir.resolve("B.class"), "b");
        Path jar = tmp.resolve("code.jar");
        writeJar(jar, 1_000);
        assertEquals(hash(tmp.resolve("classes")), hash(jar));
    }

    @Test
    void ignoresEntryTimestamps() throws IOException {
        Path first = tmp.resolve("first.jar");
        Path second = tmp.resolve("second.jar");
        writeJar(first, 1_000);
        writeJar(second, 9_000_000);
        assertEquals(hash(first), hash(second));
    }

    @Test
    void translatorCodeHashIsStable() throws IOException {
        assertEquals(ModTranslator.codeHash(ModTranslator.class), ModTranslator.codeHash(ModTranslator.class, RefmapRemapper.class));
        assertEquals(64, ModTranslator.codeHash(ModTranslator.class).length());
    }

    private static String hash(Path source) throws IOException {
        return ModTranslator.hashSources(new TreeSet<>(java.util.List.of(source)));
    }

    private static void writeJar(Path jar, long time) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (String name : new String[] {"rose/B.class", "rose/A.class"}) {
                ZipEntry e = new ZipEntry(name);
                e.setLastModifiedTime(FileTime.fromMillis(time));
                out.putNextEntry(e);
                out.write(name.equals("rose/A.class") ? "a".getBytes() : "b".getBytes());
                out.closeEntry();
            }
        }
    }
}
