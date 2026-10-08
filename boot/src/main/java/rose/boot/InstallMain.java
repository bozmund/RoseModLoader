package rose.boot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import rose.boot.mojang.GameInstaller;
import rose.boot.mojang.InstalledGame;

/**
 * Build helper: installs the Minecraft client into a run folder and writes its compile classpath (game jar +
 * libraries, one absolute path per line) so Gradle modules that use Minecraft classes can compile against it.
 *
 * <pre>java rose.boot.InstallMain &lt;runDir&gt; &lt;version&gt; &lt;classpathFile&gt;</pre>
 */
public final class InstallMain {
    public static void main(String[] args) throws Exception {
        Path runDir = Path.of(args[0]).toAbsolutePath();
        InstalledGame game = new GameInstaller(runDir).installClient(args[1]);
        List<String> lines = new ArrayList<>();
        lines.add(game.gameJar().toString());
        game.libraries().forEach(p -> lines.add(p.toString()));
        Path out = Path.of(args[2]);
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.write(out, lines);
    }

    private InstallMain() {}
}
