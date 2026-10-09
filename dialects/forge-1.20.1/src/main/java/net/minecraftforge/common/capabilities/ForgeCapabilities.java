package net.minecraftforge.common.capabilities;

import net.minecraftforge.items.IItemHandler;

/** Forge's built-in capabilities. */
public final class ForgeCapabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(IItemHandler.class);
    public static final Capability<Object> FLUID_HANDLER = new Capability<>("net.minecraftforge.fluids.capability.IFluidHandler");
    public static final Capability<Object> FLUID_HANDLER_ITEM = new Capability<>("net.minecraftforge.fluids.capability.IFluidHandlerItem");
    public static final Capability<Object> ENERGY = new Capability<>("net.minecraftforge.energy.IEnergyStorage");

    private ForgeCapabilities() {}
}
