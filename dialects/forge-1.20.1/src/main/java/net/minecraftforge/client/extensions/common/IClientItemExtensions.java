package net.minecraftforge.client.extensions.common;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

/**
 * Client-side item hooks (custom renderers, arm poses, ...). Mods hand these out from {@code Item.initializeClient},
 * which Rose calls at client setup (ClientItemExtensions); the supported parts are wired from there.
 */
public interface IClientItemExtensions {
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

    /**
     * The renderer for items whose model is {@code builtin/entity}. Forge's default was vanilla's renderer for
     * built-in items, which 26.3 replaced with special models: {@code null} here means the item has none of its own.
     */
    default BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return null;
    }
}
