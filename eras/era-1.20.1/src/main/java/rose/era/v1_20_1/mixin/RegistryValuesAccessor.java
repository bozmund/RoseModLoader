package rose.era.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Registered values before the registry is frozen (26.3 binds holder values only at freeze). */
@Mixin(MappedRegistry.class)
public interface RegistryValuesAccessor<T> {
    @Accessor("byValue")
    Map<T, Holder.Reference<T>> rose$era$byValue();
}
