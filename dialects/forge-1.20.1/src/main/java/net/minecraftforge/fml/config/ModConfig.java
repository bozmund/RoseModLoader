package net.minecraftforge.fml.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import rose.dialect.forge.v1_20_1.Toml;

/**
 * A registered mod config file. Rose keeps every type in {@code config/} (Forge puts SERVER configs in each world's
 * {@code serverconfig/}; per-world configs come later).
 */
public class ModConfig {
    public enum Type {
        COMMON,
        CLIENT,
        SERVER;

        public String extension() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final Type type;
    private final IConfigSpec<?> spec;
    private final String modId;
    private final String fileName;

    public ModConfig(Type type, IConfigSpec<?> spec, String modId, String fileName) {
        this.type = type;
        this.spec = spec;
        this.modId = modId;
        this.fileName = fileName;
    }

    public Type getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    @SuppressWarnings("unchecked")
    public <T extends IConfigSpec<T>> IConfigSpec<T> getSpec() {
        return (IConfigSpec<T>) spec;
    }

    public String getModId() {
        return modId;
    }

    public Path getFullPath() {
        return Path.of("config").resolve(fileName);
    }

    /** Reads the file if it exists, then writes it back so new options and comments show up. */
    public void load() {
        Path file = getFullPath();
        try {
            if (Files.exists(file)) spec.rose$load(Toml.read(Files.readString(file)));
            save();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load config " + file, e);
        }
    }

    public void save() {
        Path file = getFullPath();
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, spec.rose$write());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save config " + file, e);
        }
    }
}
