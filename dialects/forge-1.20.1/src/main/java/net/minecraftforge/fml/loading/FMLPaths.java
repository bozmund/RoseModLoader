package net.minecraftforge.fml.loading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Forge's well-known game paths. Rose runs with the game directory as the working directory. */
public enum FMLPaths {
    GAMEDIR(true),
    MODSDIR(true, "mods"),
    CONFIGDIR(true, "config"),
    FMLCONFIG(false, "config", "fml.toml");

    private final Path relative;
    private final boolean isDirectory;

    FMLPaths(boolean isDirectory, String... path) {
        this.relative = Path.of("", path);
        this.isDirectory = isDirectory;
    }

    public Path relative() {
        return relative;
    }

    /** The absolute path; a directory is created if missing, as FML did at startup. */
    public Path get() {
        Path path = Path.of("").toAbsolutePath().resolve(relative).normalize();
        if (isDirectory) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return path;
    }
}
