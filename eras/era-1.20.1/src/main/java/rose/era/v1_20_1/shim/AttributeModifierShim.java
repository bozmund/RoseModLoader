package rose.era.v1_20_1.shim;

import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

/** Redirect targets for 1.20.1 AttributeModifier, which was keyed by UUID; 1.21 keys modifiers by Identifier. */
public final class AttributeModifierShim {
    /** 1.20.1 Item.BASE_ATTACK_DAMAGE_UUID / BASE_ATTACK_SPEED_UUID. */
    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final Map<UUID, Identifier> KNOWN = Map.of(
            BASE_ATTACK_DAMAGE_UUID, Item.BASE_ATTACK_DAMAGE_ID,
            BASE_ATTACK_SPEED_UUID, Item.BASE_ATTACK_SPEED_ID);

    /**
     * {@code new AttributeModifier(uuid, name, amount, operation)}. Vanilla's well-known UUIDs map to the ids 26.3
     * uses for them (so tooltips still treat them as base values); others become {@code rose:legacy/<uuid>}.
     */
    public static AttributeModifier create(UUID id, String name, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(idFor(id), amount, operation);
    }

    /** {@code new AttributeModifier(name, amount, operation)}: 1.20.1 gave these a random UUID. */
    public static AttributeModifier create(String name, double amount, AttributeModifier.Operation operation) {
        return create(UUID.randomUUID(), name, amount, operation);
    }

    /** 1.20.1 {@code getName()}: modifiers no longer have a separate name; the id is the closest thing. */
    public static String getName(AttributeModifier self) {
        return self.id().toString();
    }

    public static Identifier idFor(UUID id) {
        Identifier known = KNOWN.get(id);
        return known != null ? known : Identifier.fromNamespaceAndPath("rose", "legacy/" + id.toString().toLowerCase(java.util.Locale.ROOT));
    }

    private AttributeModifierShim() {}
}
