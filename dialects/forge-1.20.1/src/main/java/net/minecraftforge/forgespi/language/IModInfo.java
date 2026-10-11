package net.minecraftforge.forgespi.language;

/** A mod's declared identity (Forge reads it from mods.toml; Rose from the mod's metadata). */
public interface IModInfo {
    String getModId();

    String getDisplayName();

    String getDescription();

    default String getNamespace() {
        return getModId();
    }
}
