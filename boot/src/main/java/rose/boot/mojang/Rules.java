package rose.boot.mojang;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Locale;

/**
 * Evaluates the "rules" blocks in Mojang's version JSON (which libraries and arguments apply to this OS).
 * Rules that depend on launcher "features" (demo mode, custom resolution, quick play) are treated as not met.
 */
final class Rules {
    static final String OS_NAME = currentOs();

    static boolean allowed(JsonObject holder) {
        JsonArray rules = holder.getAsJsonArray("rules");
        if (rules == null) return true;
        boolean allowed = false;
        for (JsonElement element : rules) {
            JsonObject rule = element.getAsJsonObject();
            if (matches(rule)) allowed = rule.get("action").getAsString().equals("allow");
        }
        return allowed;
    }

    private static boolean matches(JsonObject rule) {
        if (rule.has("features")) return false;
        JsonObject os = rule.getAsJsonObject("os");
        if (os == null) return true;
        if (os.has("name") && !os.get("name").getAsString().equals(OS_NAME)) return false;
        if (os.has("arch")) {
            String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
            if (!arch.contains(os.get("arch").getAsString())) return false;
        }
        return true;
    }

    private static String currentOs() {
        String name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (name.contains("win")) return "windows";
        if (name.contains("mac")) return "osx";
        return "linux";
    }

    private Rules() {}
}
