package rose.api.registry;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registration helpers. Call them from a {@link rose.api.ModInitializer}: Minecraft freezes its registries right
 * after mod initializers run.
 *
 * <p>Since 1.21.2, blocks and items must know their registry key before they're constructed. These helpers set it,
 * so mods don't have to.
 */
public final class RoseRegistries {
    public static <T> T register(Registry<? super T> registry, Identifier id, T value) {
        return Registry.register(registry, id, value);
    }

    public static <B extends Block> B block(Identifier id, Function<BlockBehaviour.Properties, B> factory,
                                            BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    public static <I extends Item> I item(Identifier id, Function<Item.Properties, I> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    /** Registers the usual item form of a block, under the block's id. */
    public static BlockItem blockItem(Block block, Item.Properties properties) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return item(id, p -> new BlockItem(block, p), properties.useBlockDescriptionPrefix());
    }

    public static <T extends BlockEntity> BlockEntityType<T> blockEntity(
            Identifier id, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                new BlockEntityType<>(factory, java.util.Set.of(blocks)));
    }

    private RoseRegistries() {}
}
