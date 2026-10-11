package net.minecraftforge.fml;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.ForgeModContainer;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/** Which mods are loaded. Rose-native mods count too; "forge" and "minecraft" are always present. */
public class ModList {
    private static final ModList INSTANCE = new ModList();
    private final Map<String, ModContainer> containers = new ConcurrentHashMap<>();

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return modId.equals("forge") || modId.equals("minecraft") || RoseLoader.get().mod(modId).isPresent();
    }

    public int size() {
        return RoseLoader.get().mods().size();
    }

    public List<String> getModIds() {
        return RoseLoader.get().mods().stream().map(ModMetadata::id).toList();
    }

    /** The mod's container: an FMLModContainer for a Forge mod, a plain one for any other Rose mod. */
    public Optional<? extends ModContainer> getModContainerById(String modId) {
        Optional<ModMetadata> metadata = RoseLoader.get().mod(modId);
        if (metadata.isEmpty()) return Optional.empty();
        return Optional.of(containers.computeIfAbsent(modId, id -> {
            for (ForgeModContainer mod : ForgeDialect.mods()) {
                if (mod.modId().equals(id)) return new FMLModContainer(mod);
            }
            return new ModContainer(metadata.get());
        }));
    }

    /** Every loaded mod's id and container. */
    public void forEachModContainer(java.util.function.BiConsumer<String, ModContainer> action) {
        for (String id : getModIds()) getModContainerById(id).ifPresent(container -> action.accept(id, container));
    }
}
