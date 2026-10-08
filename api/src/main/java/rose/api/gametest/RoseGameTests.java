package rose.api.gametest;

import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/**
 * Registers GameTest functions. A test also needs a test instance in the mod's data, e.g.
 * {@code data/<modid>/test_instance/<name>.json} with {@code "type": "minecraft:function"} and
 * {@code "function": "<modid>:<name>"}. Run them with {@code ./gradlew runGameTests}.
 */
public final class RoseGameTests {
    /** Call from a {@link rose.api.ModInitializer}. */
    public static void register(Identifier id, Consumer<GameTestHelper> test) {
        Registry.register(BuiltInRegistries.TEST_FUNCTION, id, test);
    }

    private RoseGameTests() {}
}
