package rose.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Loads the game, its libraries and mods, passing every class through Rose's {@link ClassTransformer}s.
 *
 * <p>Rose's own boot/loader classes and the JDK come from the parent loader so that they're shared, and
 * everything else is defined here. That gives Rose one place to rewrite any class before the JVM sees it.
 */
public final class RoseClassLoader extends SecureClassLoader {
    static {
        registerAsParallelCapable();
    }

    /**
     * Classes always taken from the parent loader, never defined (or transformed) here. Kept narrow on purpose:
     * libraries such as JNA live in {@code com.sun.jna} and must load from the game's classpath, and anything
     * not found there (e.g. {@code javax.crypto}) still falls back to the parent.
     */
    private static final List<String> PARENT_FIRST = List.of(
            "java.", "jdk.", "sun.",
            "rose.boot.", "rose.loader.");

    private final ResourceIndex resources;
    private final ClassLoader parent;
    private final List<ClassTransformer> transformers = new CopyOnWriteArrayList<>();

    public RoseClassLoader(List<Path> classpath, ClassLoader parent) {
        super("rose", parent);
        this.parent = parent;
        this.resources = new ResourceIndex(classpath);
    }

    public void addTransformer(ClassTransformer transformer) {
        transformers.add(transformer);
    }

    /** Adds a jar or folder (e.g. a mod) after start-up. */
    public void addPath(Path path) {
        resources.add(path);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = isParentFirst(name) ? parent.loadClass(name) : findOwnOrParent(name);
            }
            if (resolve) resolveClass(c);
            return c;
        }
    }

    private Class<?> findOwnOrParent(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";
        URL url = resources.findResource(path);
        if (url == null) return parent.loadClass(name);

        byte[] bytes;
        CodeSource source;
        Manifest manifest;
        try {
            URLConnection connection = url.openConnection();
            try (InputStream in = connection.getInputStream()) {
                bytes = in.readAllBytes();
            }
            source = new CodeSource(codeSourceUrl(url, connection), (Certificate[]) null);
            manifest = connection instanceof JarURLConnection jar ? jar.getManifest() : null;
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }

        String internalName = name.replace('.', '/');
        for (ClassTransformer transformer : transformers) {
            bytes = transformer.transform(internalName, bytes);
        }
        definePackageFor(name, manifest);
        return defineClass(name, bytes, 0, bytes.length, source);
    }

    private void definePackageFor(String className, Manifest manifest) {
        int dot = className.lastIndexOf('.');
        if (dot < 0) return;
        String pkg = className.substring(0, dot);
        if (getDefinedPackage(pkg) != null) return;
        try {
            // Version info from the jar manifest, as URLClassLoader does; some libraries (e.g. Log4j) read it.
            Attributes main = manifest != null ? manifest.getMainAttributes() : new Attributes();
            definePackage(pkg,
                    main.getValue(Attributes.Name.SPECIFICATION_TITLE),
                    main.getValue(Attributes.Name.SPECIFICATION_VERSION),
                    main.getValue(Attributes.Name.SPECIFICATION_VENDOR),
                    main.getValue(Attributes.Name.IMPLEMENTATION_TITLE),
                    main.getValue(Attributes.Name.IMPLEMENTATION_VERSION),
                    main.getValue(Attributes.Name.IMPLEMENTATION_VENDOR),
                    null);
        } catch (IllegalArgumentException alreadyDefinedByAnotherThread) {
            // fine: parallel loading defined it first
        }
    }

    private static URL codeSourceUrl(URL url, URLConnection connection) throws MalformedURLException {
        if (connection instanceof JarURLConnection jar) return jar.getJarFileURL();
        return url;
    }

    private static boolean isParentFirst(String name) {
        for (String prefix : PARENT_FIRST) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    public URL getResource(String name) {
        URL url = resources.findResource(name);
        return url != null ? url : parent.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> own = resources.findResources(name);
        Enumeration<URL> inherited = parent.getResources(name);
        return new Enumeration<>() {
            public boolean hasMoreElements() { return own.hasMoreElements() || inherited.hasMoreElements(); }
            public URL nextElement() { return own.hasMoreElements() ? own.nextElement() : inherited.nextElement(); }
        };
    }

    @Override
    protected URL findResource(String name) {
        return resources.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return resources.findResources(name);
    }

    /** Resource lookup over jars and folders, without letting a URLClassLoader define classes itself. */
    private static final class ResourceIndex extends URLClassLoader {
        static {
            registerAsParallelCapable();
        }

        ResourceIndex(List<Path> classpath) {
            super("rose-resources", new URL[0], null);
            classpath.forEach(this::add);
        }

        void add(Path path) {
            try {
                addURL(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
