package rose.api;

/**
 * A mod's common entrypoint, listed under {@code "main"} in {@code rose.mod.json}. Runs on both client and server,
 * after vanilla content exists and before registries are frozen: register blocks, items, payloads and GameTests here.
 */
@FunctionalInterface
public interface ModInitializer {
    void onInitialize();
}
