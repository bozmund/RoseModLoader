package rose.era.v1_20_1.shim;

import net.minecraft.resources.Identifier;

/** Redirect targets for 1.20.1 {@code ResourceLocation} constructors (26.3 Identifier has factory methods only). */
public final class IdentifierShim {
    /** {@code new ResourceLocation("ns", "path")}. */
    public static Identifier create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /** {@code new ResourceLocation("ns:path")}; no namespace means {@code minecraft}. */
    public static Identifier create(String id) {
        return Identifier.parse(id);
    }

    private IdentifierShim() {}
}
