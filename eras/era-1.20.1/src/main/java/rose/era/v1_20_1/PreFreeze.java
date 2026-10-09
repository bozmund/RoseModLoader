package rose.era.v1_20_1;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import rose.era.v1_20_1.mixin.RegistryValuesAccessor;

/** Reads registry values while registries are still open (old mods do; 26.3 binds holders only at freeze). */
public final class PreFreeze {
    /** The value of a holder, also before its registry is frozen. */
    public static Object value(Holder<?> holder) {
        if (!(holder instanceof Holder.Reference<?> ref) || ref.isBound()) return holder.value();
        Object registry = find(BuiltInRegistries.REGISTRY, r -> ((Registry<?>) r).key().identifier().equals(ref.key().registry()));
        if (registry != null) {
            Object value = find((Registry<?>) registry, v -> holderOf((Registry<?>) registry, v) == ref);
            if (value != null) return value;
        }
        return holder.value(); // throws the vanilla "unbound value" error
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object holderOf(Registry<?> registry, Object value) {
        return ((RegistryValuesAccessor) registry).rose$era$byValue().get(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object find(Registry<?> registry, java.util.function.Predicate<Object> test) {
        if (!(registry instanceof RegistryValuesAccessor accessor)) return null;
        for (Object value : accessor.rose$era$byValue().keySet()) if (test.test(value)) return value;
        return null;
    }

    private PreFreeze() {}
}
