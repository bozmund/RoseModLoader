package rose.boot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import rose.boot.mojang.GameInstaller;
import rose.boot.mojang.InstalledGame;
import rose.loader.ModDiscovery;
import rose.loader.ModMetadata;
import rose.loader.RoseClassLoader;
import rose.loader.RoseLoader;
import rose.mixin.RoseMixin;

/**
 * Rose's entry point. Installs the requested Minecraft version, builds Rose's class loader around it and
 * hands control to vanilla's own main class.
 *
 * <pre>
 * java rose.boot.RoseLaunch --side client|server [--version 26.3] [--runDir run] [--username RoseDev] [-- extra game args]
 * </pre>
 *
 * The JVM must be started with {@code --enable-native-access=ALL-UNNAMED} and
 * {@code --add-exports java.base/jdk.internal.misc=ALL-UNNAMED}, as Mojang's launcher does.
 */
public final class RoseLaunch {
    public static final String MINECRAFT_VERSION = "26.3";
    private static final String GAMETEST_MAIN = "net.minecraft.gametest.Main";

    public static void main(String[] args) throws Throwable {
        LaunchOptions options = LaunchOptions.parse(args);
        Path runDir = options.runDir().toAbsolutePath();
        GameInstaller installer = new GameInstaller(runDir);

        LaunchTarget target = options.target();
        System.out.println("[rose] Rose Mod Loader - Minecraft " + options.version() + " " + target);
        // The GameTest server ships in the client jar, so it uses the client install.
        InstalledGame game = target == LaunchTarget.SERVER
                ? installer.installServer(options.version())
                : installer.installClient(options.version());

        Path gameDir = runDir.resolve(target.folder());
        Files.createDirectories(gameDir);

        List<Path> classpath = new ArrayList<>();
        classpath.add(game.gameJar());
        classpath.addAll(game.libraries());
        RoseClassLoader loader = new RoseClassLoader(classpath, RoseLaunch.class.getClassLoader());

        List<ModMetadata> mods = ModDiscovery.discover(gameDir.resolve("mods"),
                ModDiscovery.parsePathList(System.getProperty("rose.dev.mods")));
        List<String> mixinConfigs = new ArrayList<>();
        for (ModMetadata mod : mods) {
            loader.addPath(mod.root());
            mixinConfigs.addAll(mod.mixins());
        }
        System.out.println("[rose] " + mods.size() + " mod(s): "
                + mods.stream().map(m -> m.id() + " " + m.version()).toList());
        RoseLoader.initialize(target.side(), mods, loader);
        RoseMixin.bootstrap(loader,
                target.side() == RoseLoader.Side.CLIENT ? RoseMixin.CLIENT : RoseMixin.SERVER,
                mixinConfigs);

        List<String> gameArgs = switch (target) {
            case CLIENT -> clientArgs(game, installer, gameDir, runDir, options);
            case SERVER -> new ArrayList<>(List.of("--nogui"));
            case GAMETEST -> new ArrayList<>(List.of("--universe", gameDir.resolve("universe").toString()));
        };
        gameArgs.addAll(options.extraArgs());
        String mainClass = target == LaunchTarget.GAMETEST ? GAMETEST_MAIN : game.mainClass();

        Thread.currentThread().setContextClassLoader(loader);
        Class<?> main = Class.forName(mainClass, false, loader);
        MethodHandles.publicLookup()
                .findStatic(main, "main", MethodType.methodType(void.class, String[].class))
                .invokeExact(gameArgs.toArray(String[]::new));
    }

    private static List<String> clientArgs(InstalledGame game, GameInstaller installer, Path gameDir, Path runDir,
                                           LaunchOptions options) throws Exception {
        Path natives = runDir.resolve("natives");
        // These are -D flags in Mojang's launcher; set them before the game touches LWJGL/JNA/Netty.
        System.setProperty("java.library.path", natives.resolve("java").toString());
        System.setProperty("jna.tmpdir", natives.resolve("jna").toString());
        System.setProperty("org.lwjgl.system.SharedLibraryExtractPath", natives.resolve("lwjgl").toString());
        System.setProperty("io.netty.native.workdir", natives.resolve("netty").toString());
        System.setProperty("minecraft.launcher.brand", "rose");
        System.setProperty("minecraft.launcher.version", "0.0.1");

        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + options.username()).getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "");
        Map<String, String> vars = Map.ofEntries(
                Map.entry("auth_player_name", options.username()),
                Map.entry("version_name", game.version()),
                Map.entry("game_directory", gameDir.toString()),
                Map.entry("assets_root", installer.assetsDir().toString()),
                Map.entry("assets_index_name", game.assetIndex()),
                Map.entry("auth_uuid", uuid),
                Map.entry("auth_access_token", "0"),
                Map.entry("clientid", ""),
                Map.entry("auth_xuid", ""),
                Map.entry("version_type", "Rose"));

        List<String> args = new ArrayList<>();
        JsonObject arguments = game.versionJson().getAsJsonObject("arguments");
        for (JsonElement element : arguments.getAsJsonArray("game")) {
            if (!element.isJsonPrimitive()) continue; // rule-based optional args (demo, resolution, quick play)
            args.add(substitute(element.getAsString(), vars));
        }
        return args;
    }

    private static String substitute(String template, Map<String, String> vars) {
        if (template.startsWith("${") && template.endsWith("}")) {
            String value = vars.get(template.substring(2, template.length() - 1));
            if (value != null) return value;
        }
        return template;
    }

    private RoseLaunch() {}
}
