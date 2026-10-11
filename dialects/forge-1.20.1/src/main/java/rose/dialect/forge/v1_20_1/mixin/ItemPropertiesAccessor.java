package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** For items named after construction (ProvisionalItemIds). */
@Mixin(Item.Properties.class)
public interface ItemPropertiesAccessor {
    @Accessor("id")
    void rose$setId(ResourceKey<Item> id);

    @Invoker("effectiveDescriptionId")
    String rose$effectiveDescriptionId();

    @Invoker("finalizeInitializer")
    DataComponentInitializers.Initializer<Item> rose$finalizeInitializer(Component name, Identifier model);
}
