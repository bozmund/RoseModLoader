package net.minecraftforge.fml;

import java.util.List;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/** Which mods are loaded. Rose-native mods count too; "forge" and "minecraft" are always present. */
public class ModList {
    private static final ModList INSTANCE = new ModList();

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
}
