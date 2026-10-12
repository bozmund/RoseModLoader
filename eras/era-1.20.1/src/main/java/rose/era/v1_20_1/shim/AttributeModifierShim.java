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

    /** 1.20.1 {@code getAmount()}; 1.20.5 made AttributeModifier a record ({@code amount()}). */
    public static double getAmount(AttributeModifier self) {
        return self.amount();
    }

    /** 1.20.1 {@code getOperation()}; 26.3 {@code operation()}. */
    public static AttributeModifier.Operation getOperation(AttributeModifier self) {
        return self.operation();
    }

    /** 1.20.1 {@code Operation.toValue()}: 0 add, 1 multiply base, 2 multiply total; 26.3 {@code id()}, same numbers. */
    public static int toValue(AttributeModifier.Operation self) {
        return self.id();
    }

    /**
     * 1.20.1 {@code getId()}: the UUID a modifier was created with ({@link #idFor} in reverse); a modifier made with
     * a 26.3 id gets a UUID derived from it.
     */
    public static UUID getId(AttributeModifier self) {
        for (Map.Entry<UUID, Identifier> known : KNOWN.entrySet()) {
            if (known.getValue().equals(self.id())) return known.getKey();
        }
        String path = self.id().getPath();
        if (self.id().getNamespace().equals("rose") && path.startsWith("legacy/")) {
            try {
                return UUID.fromString(path.substring("legacy/".length()));
            } catch (IllegalArgumentException e) {
                // not one Rose made from a UUID
            }
        }
        return UUID.nameUUIDFromBytes(self.id().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** 1.20.1 {@code save()}: {Name, Amount, Operation (0-2), UUID (4 ints)}; 26.3 modifiers save through a codec. */
    public static net.minecraft.nbt.CompoundTag save(AttributeModifier self) {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("Name", getName(self));
        tag.putDouble("Amount", self.amount());
        tag.putInt("Operation", self.operation().id());
        tag.putIntArray("UUID", net.minecraft.core.UUIDUtil.uuidToIntArray(getId(self)));
        return tag;
    }

    /** 1.20.1 {@code AttributeModifier.load(tag)}, what {@link #save} wrote; null (as in 1.20.1) when it isn't one. */
    public static AttributeModifier load(net.minecraft.nbt.CompoundTag tag) {
        int[] uuid = tag.getIntArray("UUID").filter(a -> a.length == 4).orElse(null);
        if (uuid == null) return null;
        return create(net.minecraft.core.UUIDUtil.uuidFromIntArray(uuid), tag.getStringOr("Name", ""), tag.getDoubleOr("Amount", 0.0),
                fromValue(tag.getIntOr("Operation", 0)));
    }

    /** 1.20.1 {@code Operation.fromValue(int)}: same numbers as 26.3's operation ids. */
    public static AttributeModifier.Operation fromValue(int value) {
        return AttributeModifier.Operation.BY_ID.apply(value);
    }

    public static Identifier idFor(UUID id) {
        Identifier known = KNOWN.get(id);
        return known != null ? known : Identifier.fromNamespaceAndPath("rose", "legacy/" + id.toString().toLowerCase(java.util.Locale.ROOT));
    }

    private AttributeModifierShim() {}
}
