package net.minecraftforge.data.event;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * Forge's data generation run. Rose never runs data generators (mods ship their generated data), so this event is
 * never posted; it exists so mods that subscribe to it still load.
 */
public class GatherDataEvent extends Event implements IModBusEvent {
    public DataGenerator getGenerator() {
        throw new UnsupportedOperationException("data generation doesn't run on Rose");
    }

    public CompletableFuture<HolderLookup.Provider> getLookupProvider() {
        throw new UnsupportedOperationException("data generation doesn't run on Rose");
    }

    public boolean includeServer() {
        return false;
    }

    public boolean includeClient() {
        return false;
    }

    public boolean includeDev() {
        return false;
    }

    public boolean includeReports() {
        return false;
    }
}
