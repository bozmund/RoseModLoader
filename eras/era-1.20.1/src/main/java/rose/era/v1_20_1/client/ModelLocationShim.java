package rose.era.v1_20_1.client;

import net.minecraft.resources.Identifier;

/**
 * 1.20.1 {@code ModelResourceLocation}, a ResourceLocation plus a variant ({@code "inventory"} for an item's model,
 * block state properties for a block's), is gone; 26.3 names models by Identifier (item models are
 * {@code ns:item/path}, ModelLocationUtils.getModelLocation). Mods' model locations become those identifiers, so they
 * still pass where a 1.20.1 ResourceLocation was expected.
 */
public final class ModelLocationShim {
    private static final String INVENTORY = "inventory";

    /** 1.20.1 {@code new ModelResourceLocation(namespace, path, variant)}. */
    public static Identifier create(String namespace, String path, String variant) {
        return create(Identifier.fromNamespaceAndPath(namespace, path), variant);
    }

    /** 1.20.1 {@code new ModelResourceLocation(id, variant)}: an item's model, or a block state's (variant dropped). */
    public static Identifier create(Identifier id, String variant) {
        return INVENTORY.equals(variant) ? id.withPrefix("item/") : id;
    }

    /** 1.20.1 {@code ModelResourceLocation.vanilla(path, variant)}. */
    public static Identifier vanilla(String path, String variant) {
        return create(Identifier.withDefaultNamespace(path), variant);
    }

    /** 1.20.1 {@code getVariant()}: what a model identifier still tells (item models were "inventory"). */
    public static String getVariant(Identifier self) {
        return self.getPath().startsWith("item/") ? INVENTORY : "";
    }

    private ModelLocationShim() {}
}
