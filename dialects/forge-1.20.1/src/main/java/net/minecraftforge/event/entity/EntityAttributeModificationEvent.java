package net.minecraftforge.event.entity;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/** Mods add attributes to existing entity types' defaults (mod bus, after EntityAttributeCreationEvent). */
public class EntityAttributeModificationEvent extends Event implements IModBusEvent {
    public record Added(EntityType<? extends LivingEntity> type, Attribute attribute, Double value) {}

    private final List<Added> added;
    private final List<EntityType<? extends LivingEntity>> types;

    public EntityAttributeModificationEvent(List<Added> added, List<EntityType<? extends LivingEntity>> types) {
        this.added = added;
        this.types = types;
    }

    public void add(EntityType<? extends LivingEntity> entityType, Attribute attribute, double value) {
        added.add(new Added(entityType, attribute, value));
    }

    public void add(EntityType<? extends LivingEntity> entityType, Attribute attribute) {
        added.add(new Added(entityType, attribute, null));
    }

    public boolean has(EntityType<? extends LivingEntity> entityType, Attribute attribute) {
        var holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
        return rose.dialect.forge.v1_20_1.ForgeAttributes.has(entityType, holder)
                || added.stream().anyMatch(a -> a.type() == entityType && a.attribute() == attribute);
    }

    public List<EntityType<? extends LivingEntity>> getTypes() {
        return types;
    }

}
