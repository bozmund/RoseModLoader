package rose.era.v1_20_1.shim;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/** Redirect targets for 1.20.1 {@code Item} members that changed type or disappeared. */
public final class ItemShim {
    public static UUID BASE_ATTACK_DAMAGE_UUID() {
        return AttributeModifierShim.BASE_ATTACK_DAMAGE_UUID;
    }

    public static UUID BASE_ATTACK_SPEED_UUID() {
        return AttributeModifierShim.BASE_ATTACK_SPEED_UUID;
    }

    /** 1.20.1 {@code Item.getFoodProperties()}; 1.20.5 moved food to the minecraft:food component. */
    public static FoodProperties getFoodProperties(Item self) {
        return self.components().get(DataComponents.FOOD);
    }

    /** 1.20.1 {@code Item.isEdible()}: the item has food. */
    public static boolean isEdible(Item self) {
        return self.components().has(DataComponents.FOOD);
    }

    private ItemShim() {}

    /** 1.20.1 {@code item.getDescription()}: the item's name; 26.3 has {@code getName(stack)} (it may depend on components). */
    public static net.minecraft.network.chat.Component getDescription(net.minecraft.world.item.Item item) {
        return net.minecraft.network.chat.Component.translatable(item.getDescriptionId());
    }

    /** Forge {@code Rarity.getStyleModifier()}: colors a name in the rarity's color. */
    public static java.util.function.UnaryOperator<net.minecraft.network.chat.Style> getStyleModifier(net.minecraft.world.item.Rarity rarity) {
        return style -> style.withColor(rarity.color());
    }
}
