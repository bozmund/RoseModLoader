package net.minecraftforge.fml;

import net.minecraftforge.forgespi.language.IModInfo;
import rose.loader.ModMetadata;

/** A loaded mod, as Forge's mod list hands it out. Every Rose mod has one (Forge mods get an FMLModContainer). */
public class ModContainer {
    private final ModMetadata metadata;
    private final IModInfo info;

    public ModContainer(ModMetadata metadata) {
        this.metadata = metadata;
        this.info = new IModInfo() {
            @Override
            public String getModId() {
                return metadata.id();
            }

            @Override
            public String getDisplayName() {
                return metadata.name();
            }

            @Override
            public String getDescription() {
                return "";
            }
        };
    }

    public String getModId() {
        return metadata.id();
    }

    public String getNamespace() {
        return metadata.id();
    }

    public IModInfo getModInfo() {
        return info;
    }
}
