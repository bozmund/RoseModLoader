package rose.api.client;

/**
 * A mod's client entrypoint, listed under {@code "client"} in {@code rose.mod.json}. Runs on the client only, after
 * every {@link rose.api.ModInitializer}, once the {@code Minecraft} instance exists.
 */
@FunctionalInterface
public interface ClientModInitializer {
    void onInitializeClient();
}
