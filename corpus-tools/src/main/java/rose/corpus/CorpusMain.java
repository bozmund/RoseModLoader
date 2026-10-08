package rose.corpus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import rose.boot.mojang.Downloader;

/**
 * Entry point for the corpus tool.
 *
 * <pre>
 * setup &lt;version&gt;...      fetch + remap + decompile each version (skips steps already done)
 * fetch &lt;version&gt;...      download and verify jars and mappings only
 * decompile &lt;version&gt;... decompile the Mojang-named client jar into source
 * inputs                  fetch everything listed in rosetta/sources.json (mappings, game versions, pilot mods)
 * </pre>
 *
 * Everything is written to {@code corpus/} in the working directory, which is git-ignored:
 * Mojang's game files and other people's mods must never be committed.
 */
public final class CorpusMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: (setup|fetch|decompile) <minecraft-version>... | inputs");
            System.exit(2);
        }
        String command = args[0];
        Corpus corpus = new Corpus(Path.of("corpus"));
        if (command.equals("inputs")) {
            fetchInputs(corpus, Path.of("rosetta", "sources.json"));
            return;
        }

        List<String> versions = Arrays.asList(args).subList(1, args.length);
        for (String version : versions) {
            switch (command) {
                case "setup" -> {
                    MinecraftVersion mc = corpus.fetch(version);
                    corpus.remapToMojangNames(mc);
                    corpus.decompile(mc);
                }
                case "fetch" -> corpus.fetch(version);
                case "decompile" -> corpus.decompile(corpus.fetch(version));
                default -> {
                    System.err.println("unknown command: " + command);
                    System.exit(2);
                }
            }
        }
        System.out.println("[corpus] done: " + versions);
    }

    /** Downloads the mapping files, game versions (jars + mappings only) and pilot mods Rosetta needs. */
    private static void fetchInputs(Corpus corpus, Path sourcesFile) throws Exception {
        JsonObject sources = JsonParser.parseString(Files.readString(sourcesFile)).getAsJsonObject();
        Downloader downloader = new Downloader();
        Path root = Path.of("corpus");
        for (JsonElement e : sources.getAsJsonArray("mappings")) {
            JsonObject m = e.getAsJsonObject();
            downloader.download(m.get("url").getAsString(), root.resolve(m.get("file").getAsString()), null);
        }
        for (JsonElement e : sources.getAsJsonArray("minecraft")) {
            corpus.fetch(e.getAsString());
        }
        for (JsonElement e : sources.getAsJsonArray("mods")) {
            JsonObject m = e.getAsJsonObject();
            downloader.download(m.get("url").getAsString(), root.resolve(m.get("file").getAsString()), m.get("sha1").getAsString());
        }
        System.out.println("[corpus] inputs ready");
    }

    private CorpusMain() {}
}
