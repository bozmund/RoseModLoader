package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** For items named after construction (ProvisionalItemIds). */
@Mixin(Item.class)
public interface ItemAccessor {
    @Mutable
    @Accessor("descriptionId")
    void rose$setDescriptionId(String descriptionId);
}
