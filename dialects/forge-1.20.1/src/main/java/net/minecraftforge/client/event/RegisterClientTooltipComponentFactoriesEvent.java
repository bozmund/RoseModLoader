package net.minecraftforge.client.event;

import java.util.function.Function;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.dialect.forge.v1_20_1.client.TooltipFactories;

/** Register how custom tooltip components are drawn (mod bus, client). */
public class RegisterClientTooltipComponentFactoriesEvent extends Event implements IModBusEvent {
    public <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory) {
        TooltipFactories.register(type, factory);
    }
}
