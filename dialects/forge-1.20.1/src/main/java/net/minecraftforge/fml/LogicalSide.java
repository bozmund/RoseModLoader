package net.minecraftforge.fml;

/** Which logical side code runs on: the client thread or the (integrated or dedicated) server. */
public enum LogicalSide {
    CLIENT,
    SERVER;

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}
