package net.minecraftforge.fml;

/** Forge's mod loader, for the state queries mods make. Rose never runs data generation. */
public final class ModLoader {
    private static final ModLoader INSTANCE = new ModLoader();

    public static ModLoader get() {
        return INSTANCE;
    }

    public static boolean isDataGenRunning() {
        return false;
    }

    public static boolean isLoadingStateValid() {
        return true;
    }

    private ModLoader() {}
}
