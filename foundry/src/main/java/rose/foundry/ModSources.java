package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.objectweb.asm.commons.Remapper;
import rose.rosetta.NameLayer;
import rose.translate.JarTranslator;
import rose.translate.RosettaRemapper;

/**
 * Decompiled source of a mod with readable 1.20.1 names (SRG names replaced by Mojang's), so an agent can read the
 * code that uses a symbol. Written to {@code corpus/foundry/src/<jar>/} (local only: it is someone else's code).
 */
final class ModSources {
    static Path dir(Path jar) {
        return Path.of("corpus", "foundry", "src", jar.getFileName().toString().replaceAll("\\.jar$", ""));
    }

    static Path prepare(Path jar, NameLayer layer) throws IOException {
        Path out = dir(jar);
        if (Files.exists(out.resolve(".complete"))) return out;
        Path readableJar = out.resolveSibling(out.getFileName() + "-readable.jar");
        Files.createDirectories(out);
        new JarTranslator(readableNames(layer)).translate(jar, readableJar);
        ConsoleDecompiler.main(new String[] {
            "--decompile-generics=1", "--remove-synthetic=1", "--log-level=error", "--skip-extra-files=1",
            readableJar.toString(), out.toString(),
        });
        Files.writeString(out.resolve(".complete"), jar.toString());
        return out;
    }

    /** SRG member names -> readable 1.20.1 names; class names are already readable in Forge mods. */
    private static Remapper readableNames(NameLayer layer) {
        return new Remapper() {
            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                if (!RosettaRemapper.SRG_METHOD.matcher(name).matches()) return name;
                NameLayer.MemberEntry e = layer.method(name);
                return e != null ? e.readableOldName() : name;
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                if (!RosettaRemapper.SRG_FIELD.matcher(name).matches()) return name;
                NameLayer.MemberEntry e = layer.field(name);
                return e != null ? e.readableOldName() : name;
            }
        };
    }

    private ModSources() {}
}
