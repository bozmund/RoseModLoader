package rose.dialect.forge.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** To copy a supplier when a mod adds attributes to an entity type (ForgeAttributes). */
@Mixin(AttributeSupplier.class)
public interface AttributeSupplierAccessor {
    @Accessor("instances")
    Map<Holder<Attribute>, AttributeInstance> rose$instances();
}
