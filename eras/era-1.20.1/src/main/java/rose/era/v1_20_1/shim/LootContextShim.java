package rose.era.v1_20_1.shim;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootTable;

/** Redirect targets for 1.20.1 {@code LootContext} members. */
public final class LootContextShim {
    /** 1.20.1 {@code getParamOrNull(param)}; 1.21.2 renamed LootContextParam to ContextKey and the getter to getOptional. */
    public static <T> T getParamOrNull(LootContext self, ContextKey<T> key) {
        return self.getOptional(key);
    }

    /** 1.20.1 {@code getResolver()}: loot tables by id; 26.3's resolver is a registry lookup (LOOT_TABLE). */
    public static LootDataResolver getResolver(LootContext self) {
        return id -> self.getResolver().lookup(Registries.LOOT_TABLE)
                .flatMap(tables -> tables.get(ResourceKey.create(Registries.LOOT_TABLE, id)))
                .map(Holder::value)
                .orElse(LootTable.EMPTY);
    }

    private LootContextShim() {}
}
