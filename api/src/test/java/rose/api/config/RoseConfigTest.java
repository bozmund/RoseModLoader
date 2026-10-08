package rose.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RoseConfigTest {
    public static final class TestConfig {
        public int limit = 5;
        public String name = "default";
    }

    @Test
    void createsFileWithDefaults(@TempDir Path dir) throws Exception {
        TestConfig config = RoseConfig.load(dir, "mod", TestConfig.class, TestConfig::new);

        assertEquals(5, config.limit);
        assertTrue(Files.readString(dir.resolve("mod.json")).contains("\"limit\": 5"));
    }

    @Test
    void readsExistingValuesAndKeepsDefaultsForMissingKeys(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("mod.json"), "{\"limit\": 9}");

        TestConfig config = RoseConfig.load(dir, "mod", TestConfig.class, TestConfig::new);

        assertEquals(9, config.limit);
        assertEquals("default", config.name);
        assertTrue(Files.readString(dir.resolve("mod.json")).contains("\"name\": \"default\""));
    }

    @Test
    void reportsInvalidJson(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("mod.json"), "{not json");
        assertThrows(IllegalStateException.class, () -> RoseConfig.load(dir, "mod", TestConfig.class, TestConfig::new));
    }
}
