package rose.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Build tool: writes a copy of a jar with access wideners applied, so a mod that relies on them compiles against
 * the same access it gets at runtime.
 *
 * <pre>java rose.loader.AccessWidenerMain in.jar out.jar file.accesswidener...</pre>
 */
public final class AccessWidenerMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) throw new IllegalArgumentException("usage: in.jar out.jar file.accesswidener...");
        AccessWidener widener = new AccessWidener();
        for (int i = 2; i < args.length; i++) widener.read(Files.readString(Path.of(args[i])), args[i]);
        Path out = Path.of(args[1]);
        Files.createDirectories(out.toAbsolutePath().getParent());
        try (ZipFile in = new ZipFile(args[0]); ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(out))) {
            var entries = in.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.isDirectory()) continue;
                byte[] bytes;
                try (InputStream stream = in.getInputStream(e)) {
                    bytes = stream.readAllBytes();
                }
                if (e.getName().endsWith(".class")) bytes = widener.transform(e.getName().substring(0, e.getName().length() - 6), bytes);
                zip.putNextEntry(new ZipEntry(e.getName()));
                zip.write(bytes);
                zip.closeEntry();
            }
        }
    }

    private AccessWidenerMain() {}
}
