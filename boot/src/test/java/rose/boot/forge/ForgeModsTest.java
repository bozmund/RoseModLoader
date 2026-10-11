package rose.boot.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
}
