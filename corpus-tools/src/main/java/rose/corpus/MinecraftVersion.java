package rose.corpus;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The local files for one Minecraft version inside {@code corpus/minecraft/<id>/}.
 *
 * @param id      version id, e.g. {@code 26.3} or {@code 1.20.1}
 * @param dir     the version's corpus folder
 */
public record MinecraftVersion(String id, Path dir) {
    public Path versionJson() { return dir.resolve("version.json"); }

    /** The client jar as Mojang ships it (obfuscated before 26.1). */
    public Path clientJar() { return dir.resolve("client.jar"); }

    /** The dedicated server bundler jar as Mojang ships it. */
    public Path serverBundlerJar() { return dir.resolve("server-bundler.jar"); }

    /** The real server jar extracted from the bundler. */
    public Path serverJar() { return dir.resolve("server.jar"); }

    /** Mojang's official ProGuard mappings (named -> obfuscated). Absent for unobfuscated versions. */
    public Path clientMappings() { return dir.resolve("client-mappings.txt"); }

    public Path serverMappings() { return dir.resolve("server-mappings.txt"); }

    /** The client jar with Mojang's real names: a remapped copy, or the original jar if unobfuscated. */
    public Path namedClientJar() { return dir.resolve("client-named.jar"); }

    /** Decompiled source of {@link #namedClientJar()}. */
    public Path sourceDir() { return dir.resolve("src"); }

    public boolean isObfuscated() { return Files.exists(clientMappings()); }
}
