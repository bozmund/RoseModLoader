package rose.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testdata.Sample;

class RoseClassLoaderTest {
    /** Copies this test's {@code Sample} class into a folder so only Rose's loader can see it there. */
    private static Path classFolderWithSample(Path dir) throws Exception {
        String path = Sample.class.getName().replace('.', '/') + ".class";
        Path target = dir.resolve(path);
        Files.createDirectories(target.getParent());
        try (var in = RoseClassLoaderTest.class.getClassLoader().getResourceAsStream(path)) {
            Files.write(target, in.readAllBytes());
        }
        return dir;
    }

    @Test
    void definesOwnClassesAndRunsTransformers(@TempDir Path dir) throws Exception {
        // Parent = platform loader, so the test class path is invisible and Rose must define Sample itself.
        RoseClassLoader loader = new RoseClassLoader(List.of(classFolderWithSample(dir)), ClassLoader.getPlatformClassLoader());
        AtomicInteger seen = new AtomicInteger();
        loader.addTransformer((name, bytes) -> {
            if (name.equals(Sample.class.getName().replace('.', '/'))) seen.incrementAndGet();
            return bytes;
        });

        Class<?> loaded = loader.loadClass(Sample.class.getName());

        assertSame(loader, loaded.getClassLoader());
        assertEquals(1, seen.get());
        assertEquals("testdata", loaded.getPackageName());
    }

    @Test
    void jdkClassesComeFromParent(@TempDir Path dir) throws Exception {
        RoseClassLoader loader = new RoseClassLoader(List.of(dir), ClassLoader.getPlatformClassLoader());
        assertSame(String.class, loader.loadClass("java.lang.String"));
    }

}
