package net.minecraftforge.fml.loading;

import java.nio.file.Path;
import net.minecraftforge.api.distmarker.Dist;

/** Forge's loader facts mods query (the side, production or development, the game directory). */
public final class FMLLoader {
    public static Dist getDist() {
        return FMLEnvironment.dist;
    }

    public static boolean isProduction() {
        return FMLEnvironment.production;
    }

    public static Path getGamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    private FMLLoader() {}
}
