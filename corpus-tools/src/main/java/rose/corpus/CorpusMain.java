package rose.corpus;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point for the corpus tool.
 *
 * <pre>
 * setup &lt;version&gt;...      fetch + remap + decompile each version (skips steps already done)
 * fetch &lt;version&gt;...      download and verify jars and mappings only
 * decompile &lt;version&gt;... decompile the Mojang-named client jar into source
 * </pre>
 *
 * Everything is written to {@code corpus/} in the working directory, which is git-ignored:
 * Mojang's game files must never be committed.
 */
public final class CorpusMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: (setup|fetch|decompile) <minecraft-version>...");
            System.exit(2);
        }
        String command = args[0];
        List<String> versions = Arrays.asList(args).subList(1, args.length);
        Corpus corpus = new Corpus(Path.of("corpus"));

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

    private CorpusMain() {}
}
