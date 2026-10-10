package rose.era.v1_20_1;

import net.minecraft.resources.RegistryOps;

/** A registry load task loading from data, with the lookup it decodes elements against (other registries, pending tags). */
public interface RegistryLoadContext {
    RegistryOps.RegistryInfoLookup rose$context();
}
