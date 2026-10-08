package rose.translate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class names, supertypes and member signatures of a set of jars, plus the JDK on demand. Enough to answer
 * "does {@code owner.name desc} exist here, possibly inherited?" without loading any class.
 */
public final class ClassIndex {
    /**
     * @param methods       {@code name+desc} of every declared method
     * @param staticMethods the subset of {@code methods} that are static
     * @param fields        {@code name:desc} and bare {@code name} of every declared field
     * @param signatures    generic signatures by {@code name+desc}, for methods that have one
     */
    public record Info(String name, String superName, List<String> interfaces, Set<String> methods, Set<String> fields,
                Set<String> methodNames, Set<String> staticMethods, Map<String, String> signatures) {}

    private final Map<String, Info> classes = new HashMap<>();
    private final boolean includeJdk;

    public ClassIndex(boolean includeJdk) {
        this.includeJdk = includeJdk;
    }

    public static ClassIndex of(List<Path> jars, boolean includeJdk) throws IOException {
        ClassIndex index = new ClassIndex(includeJdk);
        for (Path jar : jars) index.addJar(jar);
        return index;
    }

    public void addJar(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class") || name.startsWith("META-INF/") || name.endsWith("module-info.class")) continue;
                try (InputStream in = zip.getInputStream(entry)) {
                    Info info = read(in.readAllBytes());
                    classes.putIfAbsent(info.name(), info);
                }
            }
        }
    }

    public boolean contains(String name) {
        return get(name) != null;
    }

    public Set<String> names() {
        return classes.keySet();
    }

    public Info get(String name) {
        Info info = classes.get(name);
        if (info == null && includeJdk && isJdkName(name)) {
            try (InputStream in = ClassLoader.getSystemResourceAsStream(name + ".class")) {
                if (in != null) {
                    info = read(in.readAllBytes());
                    classes.put(name, info);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return info;
    }

    /** All supertypes of {@code name} (itself first), breadth-first, across this index and {@code fallback}. */
    public static List<Info> hierarchy(String name, ClassIndex first, ClassIndex fallback) {
        List<Info> out = new ArrayList<>();
        Deque<String> queue = new ArrayDeque<>(List.of(name));
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!seen.add(current)) continue;
            Info info = first.get(current);
            if (info == null && fallback != null) info = fallback.get(current);
            if (info == null) continue;
            out.add(info);
            if (info.superName() != null) queue.add(info.superName());
            queue.addAll(info.interfaces());
        }
        return out;
    }

    /** Whether a value of type {@code from} can be used where {@code to} is expected (internal names). */
    public static boolean isAssignable(String from, String to, ClassIndex first, ClassIndex fallback) {
        if (from.equals(to) || to.equals("java/lang/Object")) return true;
        return hierarchy(from, first, fallback).stream().anyMatch(i -> i.name().equals(to));
    }

    public static boolean isJdkName(String name) {
        return name.startsWith("java/") || name.startsWith("javax/") || name.startsWith("jdk/") || name.startsWith("sun/");
    }

    private static Info read(byte[] bytes) {
        Set<String> methods = new HashSet<>();
        Set<String> methodNames = new HashSet<>();
        Set<String> fields = new HashSet<>();
        Set<String> staticMethods = new HashSet<>();
        Map<String, String> signatures = new HashMap<>();
        String[] header = new String[2];
        List<String> interfaces = new ArrayList<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] ifaces) {
                header[0] = name;
                header[1] = superName;
                if (ifaces != null) interfaces.addAll(List.of(ifaces));
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                methods.add(name + desc);
                methodNames.add(name);
                if ((access & Opcodes.ACC_STATIC) != 0) staticMethods.add(name + desc);
                if (signature != null) signatures.put(name + desc, signature);
                return null;
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                fields.add(name + ":" + desc);
                fields.add(name);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return new Info(header[0], header[1], List.copyOf(interfaces), methods, fields, methodNames, staticMethods, signatures);
    }
}
