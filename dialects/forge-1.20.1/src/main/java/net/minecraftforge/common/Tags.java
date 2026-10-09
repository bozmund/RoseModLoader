package net.minecraftforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Forge's shared {@code forge:} tags. Their contents come from data packs; on 26.3 the cross-loader convention
 * moved to the {@code c:} namespace, so the dialect's data maps the old names onto it.
 */
public class Tags {
    public static class Blocks {
        public static final TagKey<Block> CHESTS = tag("chests");
        public static final TagKey<Block> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Block> FENCES = tag("fences");
        public static final TagKey<Block> FENCES_WOODEN = tag("fences/wooden");
        public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("forge", name));
        }
    }

    public static class Items {
        public static final TagKey<Item> CROPS = tag("crops");
        public static final TagKey<Item> SEEDS = tag("seeds");
        public static final TagKey<Item> SHEARS = tag("shears");
        public static final TagKey<Item> TOOLS = tag("tools");
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> MUSHROOMS = tag("mushrooms");
        public static final TagKey<Item> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden");
        public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", name));
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> IS_HOT = tag("is_hot");
        public static final TagKey<Biome> IS_HOT_OVERWORLD = tag("is_hot/overworld");
        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_WET = tag("is_wet");
        public static final TagKey<Biome> IS_WET_OVERWORLD = tag("is_wet/overworld");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_MUSHROOM = tag("is_mushroom");
        public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("forge", name));
        }
    }
}
