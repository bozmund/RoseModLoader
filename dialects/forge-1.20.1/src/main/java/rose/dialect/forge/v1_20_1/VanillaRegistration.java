package rose.dialect.forge.v1_20_1;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * Registers a Forge mod's entry into a 26.3 registry with the side effects vanilla's own registration has: a block
 * item is linked to its block ({@code Item.BY_BLOCK}, used by {@code Block.asItem()}), as {@code Items.registerItem}
 * does and Forge did for mod items. An item that sets its burn time the Forge way becomes furnace fuel (ForgeFuel).
 */
public final class VanillaRegistration {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T register(Registry<?> registry, ResourceKey<?> key, T value) {
        Registry.register((Registry) registry, (ResourceKey) key, value);
        linked(value);
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T register(Registry<?> registry, Identifier id, T value) {
        Registry.register((Registry) registry, id, value);
        linked(value);
        return value;
    }

    private static void linked(Object value) {
        if (value instanceof BlockItem blockItem) blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
        if (value instanceof Item item) ForgeFuel.register(item);
    }

    private VanillaRegistration() {}
}
