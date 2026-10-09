package net.minecraftforge.fml.loading;

import net.minecraftforge.api.distmarker.Dist;
import rose.loader.RoseLoader;

/** Facts about the running game. */
public class FMLEnvironment {
    public static final Dist dist = RoseLoader.get().side() == RoseLoader.Side.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    public static final String naming = "mcp";
    public static final boolean production = true;
    public static final boolean secureJarsEnabled = false;
}
