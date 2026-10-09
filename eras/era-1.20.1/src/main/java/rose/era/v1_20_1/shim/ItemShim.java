package rose.era.v1_20_1.shim;

import java.util.UUID;

/** Redirect targets for 1.20.1 {@code Item} members that changed type or disappeared. */
public final class ItemShim {
    public static UUID BASE_ATTACK_DAMAGE_UUID() {
        return AttributeModifierShim.BASE_ATTACK_DAMAGE_UUID;
    }

    public static UUID BASE_ATTACK_SPEED_UUID() {
        return AttributeModifierShim.BASE_ATTACK_SPEED_UUID;
    }

    private ItemShim() {}
}
