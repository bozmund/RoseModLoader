package rose.dialect.forge.v1_20_1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TomlTest {
    @Test
    void readsForgeConfigFiles() {
        Map<String, Object> values = Toml.read("""
                #General settings
                [settings]
                	#Should rope be reeled?
                	enableRopeReeling = true
                	# Range: 0.0 ~ 1.0
                	richSoilBoostChance = 0.2
                	count = 3
                	name = "farmersdelight:rope" # trailing comment
                	soups = ["minecraft:mushroom_stew",
                		"minecraft:beetroot_soup"]
                """);
        assertEquals(Map.of(
                "settings.enableRopeReeling", true,
                "settings.richSoilBoostChance", 0.2,
                "settings.count", 3L,
                "settings.name", "farmersdelight:rope",
                "settings.soups", List.of("minecraft:mushroom_stew", "minecraft:beetroot_soup")), values);
    }

    @Test
    void formatsValuesBackToToml() {
        assertEquals("[\"a\", \"b\\\"c\"]", Toml.format(List.of("a", "b\"c")));
        assertEquals("true", Toml.format(true));
        assertEquals("0.5", Toml.format(0.5));
    }
}
