package net.minecraftforge.client.extensions.common;

/**
 * Client-side item hooks (custom renderers, arm poses, ...). Mods hand these out from
 * {@code Item.initializeClient}, which 26.3 doesn't call; Rose wires the supported parts separately.
 */
public interface IClientItemExtensions {
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};
}
