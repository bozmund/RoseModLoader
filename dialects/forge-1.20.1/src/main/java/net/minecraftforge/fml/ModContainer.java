package net.minecraftforge.fml;

import net.minecraftforge.forgespi.language.IModInfo;
import rose.loader.ModMetadata;

/** A loaded mod, as Forge's mod list hands it out. Every Rose mod has one (Forge mods get an FMLModContainer). */
public class ModContainer {
    private final ModMetadata metadata;
    private final IModInfo info;
    private final java.util.Map<Class<?>, java.util.function.Supplier<?>> extensions = new java.util.concurrent.ConcurrentHashMap<>();

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

    /** Registers the mod's extension of type {@code point} (a config screen factory, a display test). */
    public <T extends Record & IExtensionPoint<T>> void registerExtensionPoint(Class<? extends T> point, java.util.function.Supplier<T> extension) {
        extensions.put(point, extension);
    }

    @SuppressWarnings("unchecked")
    public <T extends Record> java.util.Optional<T> getCustomExtension(Class<? extends T> point) {
        java.util.function.Supplier<?> extension = extensions.get(point);
        return extension == null ? java.util.Optional.empty() : java.util.Optional.of((T) extension.get());
    }
}
