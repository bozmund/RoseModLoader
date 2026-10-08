package rose.bridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Typed access to a call's JSON parameters, with clear errors for agents. */
public final class Params {
    private final JsonObject json;

    public Params(JsonObject json) {
        this.json = json != null ? json : new JsonObject();
    }

    public String string(String key) {
        return require(key).getAsString();
    }

    public String string(String key, String fallback) {
        return has(key) ? json.get(key).getAsString() : fallback;
    }

    public int integer(String key) {
        try {
            return require(key).getAsInt();
        } catch (NumberFormatException | UnsupportedOperationException e) {
            throw new InvalidParams("parameter '" + key + "' must be an integer");
        }
    }

    public int integer(String key, int fallback) {
        return has(key) ? integer(key) : fallback;
    }

    public double number(String key) {
        try {
            return require(key).getAsDouble();
        } catch (NumberFormatException | UnsupportedOperationException e) {
            throw new InvalidParams("parameter '" + key + "' must be a number");
        }
    }

    public long longValue(String key, long fallback) {
        return has(key) ? require(key).getAsLong() : fallback;
    }

    public boolean bool(String key, boolean fallback) {
        return has(key) ? require(key).getAsBoolean() : fallback;
    }

    public JsonArray array(String key) {
        return has(key) ? require(key).getAsJsonArray() : new JsonArray();
    }

    public boolean has(String key) {
        return json.has(key) && !json.get(key).isJsonNull();
    }

    private JsonElement require(String key) {
        if (!has(key)) throw new InvalidParams("missing parameter '" + key + "'");
        return json.get(key);
    }

    /** Becomes a JSON-RPC "invalid params" error. */
    public static final class InvalidParams extends RuntimeException {
        public InvalidParams(String message) {
            super(message);
        }
    }
}
