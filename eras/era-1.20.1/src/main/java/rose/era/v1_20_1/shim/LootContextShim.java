package rose.era.v1_20_1.shim;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;

/** Redirect targets for 1.20.1 {@code LootContext} members. */
public final class LootContextShim {
    /** 1.20.1 {@code getParamOrNull(param)}; 1.21.2 renamed LootContextParam to ContextKey and the getter to getOptional. */
    public static <T> T getParamOrNull(LootContext self, ContextKey<T> key) {
        return self.getOptional(key);
    }

    private LootContextShim() {}
}
