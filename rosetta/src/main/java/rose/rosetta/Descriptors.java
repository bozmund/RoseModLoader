package rose.rosetta;

import java.util.function.UnaryOperator;

/** Rewrites the class names inside JVM descriptors, e.g. {@code (La;I)Lb;} with {@code a->x, b->y}. */
public final class Descriptors {
    public static String map(String desc, UnaryOperator<String> classes) {
        StringBuilder out = new StringBuilder(desc.length());
        int i = 0;
        while (i < desc.length()) {
            char c = desc.charAt(i);
            if (c == 'L') {
                int end = desc.indexOf(';', i);
                out.append('L').append(classes.apply(desc.substring(i + 1, end))).append(';');
                i = end + 1;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private Descriptors() {}
}
