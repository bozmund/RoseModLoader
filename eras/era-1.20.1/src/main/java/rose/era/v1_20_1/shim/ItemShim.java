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

    /**
     * 1.20.1 {@code Item.isValidRepairItem(stack, repair)} reached from old code (usually an override's
     * {@code super} call): 1.20.1's Item said no; 26.3 items say what their {@code repairable} component says. Doesn't
     * dispatch to the override, which would loop for super calls.
     */
    public static boolean isValidRepairItem(Item self, net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemStack repair) {
        var repairable = stack.get(DataComponents.REPAIRABLE);
        return repairable != null && repairable.isValidRepairItem(repair);
    }

    /** {@link #isValidRepairItem(Item, net.minecraft.world.item.ItemStack, net.minecraft.world.item.ItemStack)}, called on a BlockItem. */
    public static boolean isValidRepairItem(net.minecraft.world.item.BlockItem self, net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemStack repair) {
        return isValidRepairItem((Item) self, stack, repair);
    }

    /** 1.20.1 {@code value instanceof PickaxeItem}: 26.x pickaxes are plain items in {@code #minecraft:pickaxes}. */
    public static boolean isPickaxe(Object value) {
        return value instanceof Item item && item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.PICKAXES);
    }

    /** 1.20.1 {@code value instanceof HoeItem}: 26.x hoes are plain items in {@code #minecraft:hoes}. */
    public static boolean isHoe(Object value) {
        return value instanceof Item item && item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.HOES);
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
