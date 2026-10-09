package rose.era.v1_20_1.loot;

import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import rose.era.v1_20_1.bridge.Legacy;

/**
 * Era bridge (1.20.1): LootItemConditionalFunction as 1.20.1 mods extend it (conditions as an array; the type
 * object instead of a codec). Mod subclasses are re-parented here (rules/forge-1.20.1/superclasses.tsv).
 */
public abstract class LegacyLootItemConditionalFunction extends LootItemConditionalFunction {
    protected final LootItemCondition[] predicates;

    protected LegacyLootItemConditionalFunction(LootItemCondition[] conditions) {
        super(combine(conditions));
        this.predicates = conditions;
    }

    private static Optional<Holder<LootItemCondition>> combine(LootItemCondition[] conditions) {
        if (conditions.length == 0) return Optional.empty();
        LootItemCondition combined = conditions.length == 1 ? conditions[0] : AllOfCondition.allOf(HolderSet.direct(java.util.Arrays.stream(conditions).map(Holder::direct).toList()));
        return Optional.of(Holder.direct(combined));
    }

    /** 26.3 asks the function for its codec; 1.20.1 functions name their type, whose registered codec is used. */
    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        var getType = Legacy.require(this, "getType", MethodType.methodType(LootItemFunctionType.class));
        LootItemFunctionType type = (LootItemFunctionType) Legacy.invoke(getType, this);
        return LegacyLoot.codecFor(type);
    }
}
