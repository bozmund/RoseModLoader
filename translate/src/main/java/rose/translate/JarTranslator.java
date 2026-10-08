package rose.translate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * Writes a translated copy of a mod jar: every class is renamed through a {@link Remapper}; resources, nested jars
 * and signatures-free metadata are copied as they are. (Mixin configs/refmaps and assets are handled by later
 * pipeline steps.)
 */
public final class JarTranslator {
    private final Remapper remapper;

    public JarTranslator(Remapper remapper) {
        this.remapper = remapper;
    }

    public void translate(Path input, Path output) throws IOException {
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path tmp = output.resolveSibling(output.getFileName() + ".part");
        try (ZipFile in = new ZipFile(input.toFile());
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tmp))) {
            Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || isSignature(entry.getName())) continue;
                byte[] bytes;
                try (InputStream stream = in.getInputStream(entry)) {
                    bytes = stream.readAllBytes();
                }
                if (entry.getName().endsWith(".class")) bytes = translateClass(bytes);
                out.putNextEntry(new ZipEntry(entry.getName()));
                out.write(bytes);
                out.closeEntry();
            }
        }
        Files.move(tmp, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public byte[] translateClass(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0); // same frames and maxs: renaming doesn't change control flow
        reader.accept(new ClassRemapper(writer, remapper), 0);
        return writer.toByteArray();
    }

    /** Old jar signatures would no longer match the rewritten classes. */
    private static boolean isSignature(String name) {
        if (!name.startsWith("META-INF/")) return false;
        return name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC");
    }
}
