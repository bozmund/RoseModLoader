package net.minecraftforge.client.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/** Mod bus: register key mappings (they join the controls screen and the saved options). */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
    private final List<KeyMapping> mappings = new ArrayList<>();

    public void register(KeyMapping key) {
        mappings.add(key);
    }

    public List<KeyMapping> mappings() {
        return mappings;
    }
}
