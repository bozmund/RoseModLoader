package rose.boot.mojang;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;

/**
 * A downloaded, verified Minecraft version ready to launch.
 *
 * @param gameJar    the client jar, or the real server jar extracted from the bundler
 * @param libraries  library jars for this side and OS
 * @param mainClass  vanilla's entry point for this side
 * @param assetIndex asset index id (client only, otherwise {@code null})
 */
public record InstalledGame(
        String version,
        Side side,
        Path gameJar,
        List<Path> libraries,
        String mainClass,
        JsonObject versionJson,
        String assetIndex) {

    public enum Side { CLIENT, SERVER }
}
