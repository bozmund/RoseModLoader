package rose.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModDiscoveryTest {
    private static Path mod(Path parent, String folder, String json) throws IOException {
        Path root = Files.createDirectories(parent.resolve(folder));
        Files.writeString(root.resolve(ModMetadata.FILE_NAME), json);
        return root;
    }

    @Test
    void readsModFolder(@TempDir Path dir) throws IOException {
        Path root = mod(dir, "hello", """
                {"schemaVersion": 1, "id": "hello", "version": "1.0", "mixins": ["hello.mixins.json"]}""");

        List<ModMetadata> mods = ModDiscovery.discover(dir.resolve("mods"), List.of(root));

        assertEquals(1, mods.size());
        assertEquals("hello", mods.getFirst().id());
        assertEquals("hello", mods.getFirst().name()); // defaults to id
        assertEquals(List.of("hello.mixins.json"), mods.getFirst().mixins());
    }

    @Test
    void ignoresFoldersWithoutMetadata(@TempDir Path dir) throws IOException {
        Path plain = Files.createDirectories(dir.resolve("plain"));
        assertTrue(ModDiscovery.discover(dir.resolve("mods"), List.of(plain)).isEmpty());
    }

    @Test
    void rejectsDuplicateIds(@TempDir Path dir) throws IOException {
        String json = """
                {"id": "same", "version": "1"}""";
        Path a = mod(dir, "a", json);
        Path b = mod(dir, "b", json);
        assertThrows(IOException.class, () -> ModDiscovery.discover(dir.resolve("mods"), List.of(a, b)));
    }

    @Test
    void rejectsInvalidIds(@TempDir Path dir) throws IOException {
        Path bad = mod(dir, "bad", """
                {"id": "Bad-Id", "version": "1"}""");
        assertThrows(IOException.class, () -> ModDiscovery.discover(dir.resolve("mods"), List.of(bad)));
    }
}
