package net.minecraftforge.fml.config;

import java.util.Map;

/**
 * A config specification. Forge's version is a NightConfig {@code UnmodifiableConfig}; Rose's carries what the
 * dialect needs to load and save its values.
 */
public interface IConfigSpec<T extends IConfigSpec<T>> {
    /** Puts values read from the file (dotted path to raw value) into the spec's config values. */
    void rose$load(Map<String, Object> values);

    /** The file content for the current values, with comments. */
    String rose$write();
}
