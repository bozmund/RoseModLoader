package net.minecraftforge.fml;

import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.ForgeModContainer;

/** The mod currently being loaded (Rose loads mods one at a time, on one thread). */
public class ModLoadingContext {
    private static final ModLoadingContext INSTANCE = new ModLoadingContext();

    public static ModLoadingContext get() {
        return INSTANCE;
    }

    public String getActiveNamespace() {
        ForgeModContainer active = ForgeDialect.activeMod();
        return active != null ? active.modId() : "minecraft";
    }

    public void registerConfig(ModConfig.Type type, IConfigSpec<?> spec) {
        registerConfig(type, spec, getActiveNamespace() + "-" + type.extension() + ".toml");
    }

    public void registerConfig(ModConfig.Type type, IConfigSpec<?> spec, String fileName) {
        ForgeModContainer active = ForgeDialect.activeMod();
        if (active == null) throw new IllegalStateException("registerConfig called outside of mod loading");
        ModConfig config = new ModConfig(type, spec, active.modId(), fileName);
        active.configs().add(config);
        config.load();
    }
}
