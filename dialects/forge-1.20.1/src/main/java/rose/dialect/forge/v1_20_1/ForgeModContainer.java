package rose.dialect.forge.v1_20_1;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import rose.loader.ModMetadata;

/** One loaded Forge mod: its metadata, its mod event bus, its configs and its constructed {@code @Mod} objects. */
public final class ForgeModContainer {
    private final ModMetadata metadata;
    private final RoseEventBus modBus;
    private final List<ModConfig> configs = new ArrayList<>();
    private final List<Object> instances = new ArrayList<>();

    public ForgeModContainer(ModMetadata metadata) {
        this.metadata = metadata;
        this.modBus = new RoseEventBus(metadata.id());
    }

    public String modId() {
        return metadata.id();
    }

    public ModMetadata metadata() {
        return metadata;
    }

    public IEventBus modBus() {
        return modBus;
    }

    public List<ModConfig> configs() {
        return configs;
    }

    public List<Object> instances() {
        return instances;
    }
}
