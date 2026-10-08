package rose.bridge;

import com.google.gson.JsonElement;

/**
 * One operation an agent can call.
 *
 * @param name        e.g. {@code world.getBlock}
 * @param params      human/AI-readable parameter summary, e.g. {@code "x:int, y:int, z:int, dimension?:string"}
 * @param description what it does and returns
 */
public record BridgeMethod(String name, String params, String description, Handler handler) {
    @FunctionalInterface
    public interface Handler {
        JsonElement call(Params params) throws Exception;
    }
}
