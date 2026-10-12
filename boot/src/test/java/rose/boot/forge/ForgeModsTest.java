package rose.boot.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ForgeModsTest {
    @Test
    void firstModReadsKeysOfTheFirstModsTable() {
        String toml = """
                modLoader="javafml"
                [[mods]] #mandatory
                modId="mysterious_mountain_lib" # its id
                version="${file.jarVersion}"
                description='''
                A library.
                '''
                displayName='Mysterious Mountain Lib'
                [[dependencies.mysterious_mountain_lib]]
                    modId="forge"
                """;
        var mod = ForgeMods.firstMod(toml);
        assertEquals("mysterious_mountain_lib", mod.get("modId"));
        assertEquals("Mysterious Mountain Lib", mod.get("displayName"));
        assertEquals("${file.jarVersion}", mod.get("version"));
    }

    /**
     * A server translates against the server jar, a client against the client jar (which has the client classes that
     * access transformers and call adaptation look up), so their translated jars mustn't share a cache key.
     */
    @Test
    void theGameJarsArePartOfTheTranslationKey(@TempDir Path dir) throws IOException {
        Path client = Files.write(dir.resolve("client.jar"), new byte[20]);
        Path server = Files.write(dir.resolve("server.jar"), new byte[10]);
        Path library = Files.write(dir.resolve("library.jar"), new byte[5]);
        String clientKey = ForgeMods.gameJarsKey(List.of(client, library));
        assertEquals(clientKey, ForgeMods.gameJarsKey(List.of(client, library)));
        assertNotEquals(clientKey, ForgeMods.gameJarsKey(List.of(server, library)));
        Files.write(client, new byte[21]); // another game version
        assertNotEquals(clientKey, ForgeMods.gameJarsKey(List.of(client, library)));
    }
}
