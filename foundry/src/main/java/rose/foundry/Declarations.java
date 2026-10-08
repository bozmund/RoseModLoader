package rose.foundry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Finds which classes in a type's hierarchy actually declare a method with a given name, so a context pack can
 * show the real source (e.g. {@code BlockState.is} is declared in {@code BlockBehaviour$BlockStateBase}).
 */
final class Declarations {
    static List<String> declaringClasses(Path jar, String owner, String methodName, int max) throws IOException {
        List<String> out = new ArrayList<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Deque<String> queue = new ArrayDeque<>(List.of(owner));
            Set<String> seen = new HashSet<>();
            while (!queue.isEmpty() && out.size() < max) {
                String current = queue.removeFirst();
                if (!seen.add(current)) continue;
                ZipEntry entry = zip.getEntry(current + ".class");
                if (entry == null) continue;
                ClassNode node = new ClassNode();
                try (InputStream in = zip.getInputStream(entry)) {
                    new ClassReader(in.readAllBytes()).accept(node, ClassReader.SKIP_CODE);
                }
                for (MethodNode m : node.methods) {
                    if (m.name.equals(methodName)) {
                        out.add(current);
                        break;
                    }
                }
                if (node.superName != null) queue.add(node.superName);
                queue.addAll(node.interfaces);
            }
        }
        return out;
    }

    private Declarations() {}
}
