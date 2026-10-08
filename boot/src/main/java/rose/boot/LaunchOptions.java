package rose.boot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Command-line options for {@link RoseLaunch}. Arguments after {@code --} go to the game unchanged. */
record LaunchOptions(LaunchTarget target, String version, Path runDir, String username, List<String> extraArgs) {

    static LaunchOptions parse(String[] args) {
        LaunchTarget target = LaunchTarget.CLIENT;
        String version = RoseLaunch.MINECRAFT_VERSION;
        Path runDir = Path.of("run");
        String username = "RoseDev";
        List<String> extra = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--side" -> target = LaunchTarget.valueOf(args[++i].toUpperCase(Locale.ROOT));
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
        return new LaunchOptions(target, version, runDir, username, List.copyOf(extra));
    }
}
