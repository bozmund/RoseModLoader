package rose.boot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import rose.boot.mojang.InstalledGame;

/** Command-line options for {@link RoseLaunch}. Arguments after {@code --} go to the game unchanged. */
record LaunchOptions(InstalledGame.Side side, String version, Path runDir, String username, List<String> extraArgs) {

    static LaunchOptions parse(String[] args) {
        InstalledGame.Side side = InstalledGame.Side.CLIENT;
        String version = RoseLaunch.MINECRAFT_VERSION;
        Path runDir = Path.of("run");
        String username = "RoseDev";
        List<String> extra = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--side" -> side = InstalledGame.Side.valueOf(args[++i].toUpperCase(Locale.ROOT));
                case "--version" -> version = args[++i];
                case "--runDir" -> runDir = Path.of(args[++i]);
                case "--username" -> username = args[++i];
                case "--" -> {
                    extra.addAll(List.of(args).subList(i + 1, args.length));
                    i = args.length;
                }
                default -> throw new IllegalArgumentException("unknown option: " + args[i]);
            }
        }
        return new LaunchOptions(side, version, runDir, username, List.copyOf(extra));
    }
}
