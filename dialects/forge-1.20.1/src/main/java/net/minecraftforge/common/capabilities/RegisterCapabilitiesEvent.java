package net.minecraftforge.common.capabilities;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/** Mods declare their capability types (mod bus, after construction). */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {
    public <T> void register(Class<T> type) {
        CapabilityManager.get(type);
    }
}
