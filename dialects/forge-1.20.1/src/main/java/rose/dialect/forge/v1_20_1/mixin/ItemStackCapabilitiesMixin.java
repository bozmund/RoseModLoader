package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import rose.dialect.forge.v1_20_1.CapabilityHolder;
import rose.dialect.forge.v1_20_1.ForgeItemHooks;

/**
 * Forge 1.20.1 item stack capabilities: the item's initCapabilities plus AttachCapabilitiesEvent&lt;ItemStack&gt;. Gathered
 * when first asked for (Forge gathered them as the stack was created; stacks are made long before anyone asks).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackCapabilitiesMixin implements ICapabilityProvider {
    @Unique
    private CapabilityHolder rose$caps;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) return LazyOptional.empty();
        if (rose$caps == null) rose$caps = CapabilityHolder.gather(ItemStack.class, self, ForgeItemHooks.initCapabilities(self));
        return rose$caps.getCapability(cap, side);
    }
}
