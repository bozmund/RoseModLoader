package rose.era.v1_20_1.shim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import rose.era.v1_20_1.RegistrationContext;

/** Redirect targets for 1.20.1 {@code EntityType.Builder}. */
public final class EntityTypeShim {
    /**
     * 1.20.1 {@code build(String)} took a name used only for data fixers; 26.3 {@code build(ResourceKey)} needs the
     * entity's registry key. During registration the key being registered is known; otherwise the name is used.
     */
    public static <T extends Entity> EntityType<T> build(EntityType.Builder<T> self, String name) {
        ResourceKey<EntityType<?>> key = RegistrationContext.current(Registries.ENTITY_TYPE);
        if (key == null) key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse(name));
        return self.build(key);
    }

    private EntityTypeShim() {}
}
