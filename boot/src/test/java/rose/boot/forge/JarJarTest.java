package rose.boot.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JarJarTest {
    @TempDir
    Path tmp;

    @Test
    void versionsCompareNumerically() {
        assertTrue(JarJar.compare("0.13.15+1.20.1", "0.13.9+1.20.1") > 0);
        assertTrue(JarJar.compare("20", "20.0") == 0);
        assertTrue(JarJar.compare("1.2", "1.10") < 0);
    }

    @Test
    void nestedJarsAreExtractedOnceWithTheHighestVersion() throws IOException {
        byte[] libOld = jar("lib", null);
        byte[] libNew = jar("lib", null);
        byte[] dep = jar("dep", "depmod");
        Path a = write("a.jar", jar("a", "amod", nested("g:lib", "1.0", "lib-1.0.jar", libOld), nested("g:dep", "2", "dep-2.jar", dep)));
        Path b = write("b.jar", jar("b", "bmod", nested("g:lib", "1.2", "lib-1.2.jar", libNew)));
        List<Path> all = JarJar.expand(List.of(a, b), tmp.resolve("jarjar"));
        assertEquals(List.of("a.jar", "b.jar", "lib-1.2.jar", "dep-2.jar"), all.stream().map(p -> p.getFileName().toString()).toList());
    }

    @Test
    void aNestedModAlreadyInstalledIsSkipped() throws IOException {
        Path dep = write("dep.jar", jar("dep", "depmod"));
        Path a = write("a.jar", jar("a", "amod", nested("g:dep", "2", "dep-2.jar", jar("dep", "depmod"))));
        assertEquals(List.of(a, dep), JarJar.expand(List.of(a, dep), tmp.resolve("jarjar")));
    }

    private record Entry(String artifact, String version, String file, byte[] bytes) {}

    private static Entry nested(String artifact, String version, String file, byte[] bytes) {
        return new Entry(artifact, version, file, bytes);
    }

    private Path write(String name, byte[] bytes) throws IOException {
        return Files.write(tmp.resolve(name), bytes);
    }

    /** A jar with mods.toml (when {@code modId} is set) and the given nested jars. */
    private static byte[] jar(String name, String modId, Entry... nested) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            out.putNextEntry(new ZipEntry(name + ".txt"));
            out.write(name.getBytes(StandardCharsets.UTF_8));
            if (modId != null) {
                out.putNextEntry(new ZipEntry("META-INF/mods.toml"));
                out.write(("[[mods]]\nmodId=\"" + modId + "\"\n").getBytes(StandardCharsets.UTF_8));
            }
            if (nested.length > 0) {
                StringBuilder json = new StringBuilder("{\"jars\":[");
                for (int i = 0; i < nested.length; i++) {
                    Entry e = nested[i];
                    String[] id = e.artifact().split(":");
                    if (i > 0) json.append(',');
                    json.append("{\"identifier\":{\"group\":\"").append(id[0]).append("\",\"artifact\":\"").append(id[1])
                            .append("\"},\"version\":{\"range\":\"[0,)\",\"artifactVersion\":\"").append(e.version())
                            .append("\"},\"path\":\"META-INF/jarjar/").append(e.file()).append("\",\"isObfuscated\":false}");
                    out.putNextEntry(new ZipEntry("META-INF/jarjar/" + e.file()));
                    out.write(e.bytes());
                }
                json.append("]}");
                out.putNextEntry(new ZipEntry("META-INF/jarjar/metadata.json"));
                out.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        return bytes.toByteArray();
    }
}
