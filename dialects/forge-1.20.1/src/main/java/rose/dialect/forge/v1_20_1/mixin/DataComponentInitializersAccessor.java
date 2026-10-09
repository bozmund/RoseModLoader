package rose.dialect.forge.v1_20_1.mixin;

import java.util.List;
import net.minecraft.core.component.DataComponentInitializers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Item constructors add component initializers; a registration that fails afterwards must take them back. */
@Mixin(DataComponentInitializers.class)
public interface DataComponentInitializersAccessor {
    @Accessor("initializers")
    List<?> rose$initializers();
}
