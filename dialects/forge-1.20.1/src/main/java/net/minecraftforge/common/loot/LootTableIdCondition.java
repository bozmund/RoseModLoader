package net.minecraftforge.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import rose.dialect.forge.v1_20_1.ForgeLootModifiers;

/**
 * Forge's {@code forge:loot_table_id} loot condition: the loot table being rolled has this id. Global loot modifiers
 * use it to pick the tables they change (Forge set the id on the LootContext; Rose knows it while modifiers run).
 */
public record LootTableIdCondition(Identifier targetLootTableId) implements LootItemCondition {
    public static final MapCodec<LootTableIdCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("loot_table_id").forGetter(LootTableIdCondition::targetLootTableId))
            .apply(i, LootTableIdCondition::new));

    /** Registers the condition type; called before vanilla freezes its registries. */
    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Identifier.fromNamespaceAndPath("forge", "loot_table_id"), MAP_CODEC);
    }

    @Override
    public MapCodec<LootTableIdCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        return targetLootTableId.equals(ForgeLootModifiers.queriedLootTable());
    }
}
