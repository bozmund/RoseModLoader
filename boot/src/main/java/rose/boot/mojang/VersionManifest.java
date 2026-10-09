package rose.boot.mojang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Mojang's list of every Minecraft version, used to find a version's JSON file. */
public final class VersionManifest {
    public static final String URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final Downloader downloader;
    private JsonObject manifest;

    public VersionManifest(Downloader downloader) {
        this.downloader = downloader;
    }

    /**
     * Downloads (or reuses) the version JSON for {@code id} into {@code target} and parses it.
     */
    public JsonObject versionJson(String id, Path target) throws IOException, InterruptedException {
        try {
            JsonObject entry = entry(id);
            downloader.download(entry.get("url").getAsString(), target, entry.get("sha1").getAsString());
        } catch (IOException offline) {
            // Released versions don't change: a cached version JSON is fine without the network.
            if (!Files.exists(target)) throw offline;
            System.out.println("[rose] offline, using cached " + target.getFileName() + " (" + offline + ")");
        }
        return JsonParser.parseString(Files.readString(target)).getAsJsonObject();
    }

    private JsonObject entry(String id) throws IOException, InterruptedException {
        if (manifest == null) {
            manifest = JsonParser.parseString(downloader.getString(URL)).getAsJsonObject();
        }
        for (var element : manifest.getAsJsonArray("versions")) {
            JsonObject entry = element.getAsJsonObject();
            if (entry.get("id").getAsString().equals(id)) return entry;
        }
        throw new IOException("Minecraft version not found in Mojang's manifest: " + id);
    }
}
