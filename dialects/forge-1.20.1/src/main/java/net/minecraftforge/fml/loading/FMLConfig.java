package net.minecraftforge.fml.loading;

/** FML's own settings (fml.toml); Rose has the defaults only. */
public final class FMLConfig {
    /** Where modpacks put default configs copied into new worlds (Forge's default). */
    public static String defaultConfigPath() {
        return "defaultconfigs";
    }

    private FMLConfig() {}
}
