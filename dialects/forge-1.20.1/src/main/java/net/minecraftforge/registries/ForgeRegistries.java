package net.minecraftforge.registries;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import rose.dialect.forge.v1_20_1.ForgeRegistryLookup;

/** Forge's registry handles. Names follow Forge 1.20.1; each maps to the 26.3 registry with the same id. */
public class ForgeRegistries {
    public static final IForgeRegistry<Block> BLOCKS = ForgeRegistryLookup.vanilla("block");
    public static final IForgeRegistry<Fluid> FLUIDS = ForgeRegistryLookup.vanilla("fluid");
    public static final IForgeRegistry<Item> ITEMS = ForgeRegistryLookup.vanilla("item");
    public static final IForgeRegistry<MobEffect> MOB_EFFECTS = ForgeRegistryLookup.vanilla("mob_effect");
    public static final IForgeRegistry<SoundEvent> SOUND_EVENTS = ForgeRegistryLookup.vanilla("sound_event");
    public static final IForgeRegistry<Potion> POTIONS = ForgeRegistryLookup.vanilla("potion");
    public static final IForgeRegistry<Object> ENCHANTMENTS = ForgeRegistryLookup.vanilla("enchantment");
    public static final IForgeRegistry<EntityType<?>> ENTITY_TYPES = ForgeRegistryLookup.vanilla("entity_type");
    public static final IForgeRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = ForgeRegistryLookup.vanilla("block_entity_type");
    public static final IForgeRegistry<ParticleType<?>> PARTICLE_TYPES = ForgeRegistryLookup.vanilla("particle_type");
    public static final IForgeRegistry<MenuType<?>> MENU_TYPES = ForgeRegistryLookup.vanilla("menu");
    public static final IForgeRegistry<Object> PAINTING_VARIANTS = ForgeRegistryLookup.vanilla("painting_variant");
    public static final IForgeRegistry<RecipeType<?>> RECIPE_TYPES = ForgeRegistryLookup.vanilla("recipe_type");
    public static final IForgeRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ForgeRegistryLookup.vanilla("recipe_serializer");
    public static final IForgeRegistry<Attribute> ATTRIBUTES = ForgeRegistryLookup.vanilla("attribute");
    public static final IForgeRegistry<Object> STAT_TYPES = ForgeRegistryLookup.vanilla("stat_type");
    public static final IForgeRegistry<Object> COMMAND_ARGUMENT_TYPES = ForgeRegistryLookup.vanilla("command_argument_type");
    public static final IForgeRegistry<VillagerProfession> VILLAGER_PROFESSIONS = ForgeRegistryLookup.vanilla("villager_profession");
    public static final IForgeRegistry<Object> POI_TYPES = ForgeRegistryLookup.vanilla("point_of_interest_type");
    public static final IForgeRegistry<Object> MEMORY_MODULE_TYPES = ForgeRegistryLookup.vanilla("memory_module_type");
    public static final IForgeRegistry<Object> SENSOR_TYPES = ForgeRegistryLookup.vanilla("sensor_type");
    public static final IForgeRegistry<Object> SCHEDULES = ForgeRegistryLookup.vanilla("schedule");
    public static final IForgeRegistry<Object> ACTIVITIES = ForgeRegistryLookup.vanilla("activity");
    public static final IForgeRegistry<Object> WORLD_CARVERS = ForgeRegistryLookup.vanilla("worldgen/carver");
    public static final IForgeRegistry<Feature> FEATURES = ForgeRegistryLookup.vanilla("worldgen/feature");
    public static final IForgeRegistry<Object> CHUNK_STATUS = ForgeRegistryLookup.vanilla("chunk_status");
    public static final IForgeRegistry<Object> BLOCK_STATE_PROVIDER_TYPES = ForgeRegistryLookup.vanilla("worldgen/block_state_provider_type");
    public static final IForgeRegistry<Object> FOLIAGE_PLACER_TYPES = ForgeRegistryLookup.vanilla("worldgen/foliage_placer_type");
    public static final IForgeRegistry<Object> TREE_DECORATOR_TYPES = ForgeRegistryLookup.vanilla("worldgen/tree_decorator_type");
    public static final IForgeRegistry<Biome> BIOMES = ForgeRegistryLookup.vanilla("worldgen/biome");

    // Forge's own registries (Forge exposes them as suppliers).
    public static final Supplier<IForgeRegistry<Object>> ENTITY_DATA_SERIALIZERS = () -> ForgeRegistryLookup.get(Keys.ENTITY_DATA_SERIALIZERS);
    public static final Supplier<IForgeRegistry<Codec<?>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = () -> ForgeRegistryLookup.get(Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
    public static final Supplier<IForgeRegistry<Codec<?>>> BIOME_MODIFIER_SERIALIZERS = () -> ForgeRegistryLookup.get(Keys.BIOME_MODIFIER_SERIALIZERS);
    public static final Supplier<IForgeRegistry<Codec<?>>> STRUCTURE_MODIFIER_SERIALIZERS = () -> ForgeRegistryLookup.get(Keys.STRUCTURE_MODIFIER_SERIALIZERS);
    public static final Supplier<IForgeRegistry<Object>> FLUID_TYPES = () -> ForgeRegistryLookup.get(Keys.FLUID_TYPES);
    public static final Supplier<IForgeRegistry<Object>> HOLDER_SET_TYPES = () -> ForgeRegistryLookup.get(Keys.HOLDER_SET_TYPES);
    public static final Supplier<IForgeRegistry<Object>> DISPLAY_CONTEXTS = () -> ForgeRegistryLookup.get(Keys.DISPLAY_CONTEXTS);

    public static final class Keys {
        public static final ResourceKey<Registry<Block>> BLOCKS = vanilla("block");
        public static final ResourceKey<Registry<Fluid>> FLUIDS = vanilla("fluid");
        public static final ResourceKey<Registry<Item>> ITEMS = vanilla("item");
        public static final ResourceKey<Registry<MobEffect>> MOB_EFFECTS = vanilla("mob_effect");
        public static final ResourceKey<Registry<Potion>> POTIONS = vanilla("potion");
        public static final ResourceKey<Registry<Object>> ENCHANTMENTS = vanilla("enchantment");
        public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPES = vanilla("entity_type");
        public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = vanilla("block_entity_type");
        public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPES = vanilla("particle_type");
        public static final ResourceKey<Registry<MenuType<?>>> MENU_TYPES = vanilla("menu");
        public static final ResourceKey<Registry<Object>> PAINTING_VARIANTS = vanilla("painting_variant");
        public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPES = vanilla("recipe_type");
        public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZERS = vanilla("recipe_serializer");
        public static final ResourceKey<Registry<Attribute>> ATTRIBUTES = vanilla("attribute");
        public static final ResourceKey<Registry<Object>> STAT_TYPES = vanilla("stat_type");
        public static final ResourceKey<Registry<Object>> COMMAND_ARGUMENT_TYPES = vanilla("command_argument_type");
        public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSIONS = vanilla("villager_profession");
        public static final ResourceKey<Registry<Object>> POI_TYPES = vanilla("point_of_interest_type");
        public static final ResourceKey<Registry<Object>> MEMORY_MODULE_TYPES = vanilla("memory_module_type");
        public static final ResourceKey<Registry<Object>> SENSOR_TYPES = vanilla("sensor_type");
        public static final ResourceKey<Registry<Object>> SCHEDULES = vanilla("schedule");
        public static final ResourceKey<Registry<Object>> ACTIVITIES = vanilla("activity");
        public static final ResourceKey<Registry<Object>> WORLD_CARVERS = vanilla("worldgen/carver");
        public static final ResourceKey<Registry<Feature>> FEATURES = vanilla("worldgen/feature");
        public static final ResourceKey<Registry<Object>> CHUNK_STATUS = vanilla("chunk_status");
        public static final ResourceKey<Registry<Object>> BLOCK_STATE_PROVIDER_TYPES = vanilla("worldgen/block_state_provider_type");
        public static final ResourceKey<Registry<Object>> FOLIAGE_PLACER_TYPES = vanilla("worldgen/foliage_placer_type");
        public static final ResourceKey<Registry<Object>> TREE_DECORATOR_TYPES = vanilla("worldgen/tree_decorator_type");
        public static final ResourceKey<Registry<Biome>> BIOMES = vanilla("worldgen/biome");

        public static final ResourceKey<Registry<Object>> ENTITY_DATA_SERIALIZERS = forge("entity_data_serializers");
        public static final ResourceKey<Registry<Codec<?>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = forge("global_loot_modifier_serializers");
        public static final ResourceKey<Registry<Codec<?>>> BIOME_MODIFIER_SERIALIZERS = forge("biome_modifier_serializers");
        public static final ResourceKey<Registry<Codec<?>>> STRUCTURE_MODIFIER_SERIALIZERS = forge("structure_modifier_serializers");
        public static final ResourceKey<Registry<Object>> FLUID_TYPES = forge("fluid_type");
        public static final ResourceKey<Registry<Object>> HOLDER_SET_TYPES = forge("holder_set_type");
        public static final ResourceKey<Registry<Object>> DISPLAY_CONTEXTS = forge("display_contexts");
        public static final ResourceKey<Registry<Object>> BIOME_MODIFIERS = forge("biome_modifier");
        public static final ResourceKey<Registry<Object>> STRUCTURE_MODIFIERS = forge("structure_modifier");

        private static <T> ResourceKey<Registry<T>> vanilla(String path) {
            return ResourceKey.createRegistryKey(Identifier.withDefaultNamespace(path));
        }

        private static <T> ResourceKey<Registry<T>> forge(String path) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("forge", path));
        }
    }
}
