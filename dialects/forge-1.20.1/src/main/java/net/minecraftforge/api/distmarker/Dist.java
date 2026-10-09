package net.minecraftforge.api.distmarker;

/** Which physical side (game jar) is running. */
public enum Dist {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isDedicatedServer() {
        return this == DEDICATED_SERVER;
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}
