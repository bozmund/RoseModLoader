package rose.dialect.forge.v1_20_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import rose.dialect.forge.v1_20_1.mixin.AttributeSupplierAccessor;

/**
 * Default attributes from Forge mods (ForgeHooks' attribute events): suppliers for mod entity types, and attributes
 * added to existing types. DefaultAttributesMixin consults these before vanilla's fixed map.
 */
public final class ForgeAttributes {
    private static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS = new HashMap<>();

    /** After registration: EntityAttributeCreationEvent, then EntityAttributeModificationEvent. */
    static void gather() {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier> created = new HashMap<>();
        ForgeDialect.postToMods(new EntityAttributeCreationEvent(created), "EntityAttributeCreationEvent");
        SUPPLIERS.putAll(created);
        List<EntityType<? extends LivingEntity>> types = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            @SuppressWarnings("unchecked")
            EntityType<? extends LivingEntity> living = (EntityType<? extends LivingEntity>) type;
            if (supplier(living) != null) types.add(living);
        }
        List<EntityAttributeModificationEvent.Added> added = new ArrayList<>();
        ForgeDialect.postToMods(new EntityAttributeModificationEvent(added, types), "EntityAttributeModificationEvent");
        Map<EntityType<? extends LivingEntity>, List<EntityAttributeModificationEvent.Added>> byType = new HashMap<>();
        for (var a : added) byType.computeIfAbsent(a.type(), k -> new ArrayList<>()).add(a);
        byType.forEach((type, additions) -> {
            AttributeSupplier base = supplier(type);
            AttributeSupplier.Builder builder = AttributeSupplier.builder();
            Map<Holder<Attribute>, Double> values = new HashMap<>();
            if (base != null) ((AttributeSupplierAccessor) base).rose$instances().forEach((h, i) -> values.put(h, i.getBaseValue()));
            for (var a : additions) {
                Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(a.attribute());
                values.put(holder, a.value() != null ? a.value() : a.attribute().getDefaultValue());
            }
            values.forEach(builder::add);
            SUPPLIERS.put(type, builder.build());
        });
    }

    /** A Forge mod's supplier for {@code type}, or vanilla's. */
    private static AttributeSupplier supplier(EntityType<? extends LivingEntity> type) {
        AttributeSupplier forge = SUPPLIERS.get(type);
        return forge != null ? forge : DefaultAttributes.hasSupplier(type) ? DefaultAttributes.getSupplier(type) : null;
    }

    public static AttributeSupplier get(EntityType<?> type) {
        return SUPPLIERS.get(type);
    }

    public static boolean has(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
        AttributeSupplier supplier = supplier(type);
        return supplier != null && supplier.hasAttribute(attribute);
    }

    private ForgeAttributes() {}
}
