package net.minecraftforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.ToolAction;
import rose.dialect.forge.v1_20_1.shim.ForgeVanillaShim;

/**
 * Forge's {@code forge:can_tool_perform_action} loot condition: the tool used can perform the action (e.g. a knife
 * harvesting straw). Packfix rewrites old loot tables to 26.3's shape ({@code type}, {@code action}).
 */
public record CanToolPerformAction(ToolAction action) implements LootItemCondition {
    public static final MapCodec<CanToolPerformAction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.xmap(ToolAction::get, ToolAction::name).fieldOf("action").forGetter(CanToolPerformAction::action))
            .apply(i, CanToolPerformAction::new));

    /** Registers Forge's loot condition types; called before vanilla freezes its registries. */
    public static void register() {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Identifier.fromNamespaceAndPath("forge", "can_tool_perform_action"), MAP_CODEC);
    }

    @Override
    public MapCodec<CanToolPerformAction> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext context) {
        ItemInstance tool = context.getOptional(LootContextParams.TOOL);
        return tool instanceof ItemStack stack && ForgeVanillaShim.canPerformAction(stack, action);
    }
}
