package rose.core.pack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/**
 * One required, always-enabled pack per mod that has {@code data/} (server) or {@code assets/} (client) content.
 * Mods don't need a {@code pack.mcmeta}; Rose supplies the metadata.
 */
public final class ModPackSource implements RepositorySource {
    private final PackType type;

    public ModPackSource(PackType type) {
        this.type = type;
    }

    @Override
    public void loadPacks(Consumer<Pack> result) {
        for (ModMetadata mod : RoseLoader.get().mods()) {
            if (!hasContent(mod.root(), type.getDirectory())) continue;
            PackLocationInfo location = new PackLocationInfo(
                    "mod/" + mod.id(), Component.literal(mod.name()), PackSource.BUILT_IN, Optional.empty());
            Pack.ResourcesSupplier resources = Files.isDirectory(mod.root())
                    ? new PathPackResources.PathResourcesSupplier(mod.root())
                    : new FilePackResources.FileResourcesSupplier(mod.root());
            Pack.Metadata metadata = new Pack.Metadata(
                    Component.literal(mod.name() + " " + mod.version()), PackCompatibility.COMPATIBLE,
                    FeatureFlagSet.of(), List.of());
            result.accept(new Pack(location, resources, metadata,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)));
        }
    }

    private static boolean hasContent(Path root, String directory) {
        if (Files.isDirectory(root)) return Files.isDirectory(root.resolve(directory));
        try (ZipFile zip = new ZipFile(root.toFile())) {
            return zip.stream().anyMatch(e -> e.getName().startsWith(directory + "/"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read mod " + root, e);
        }
    }
}
