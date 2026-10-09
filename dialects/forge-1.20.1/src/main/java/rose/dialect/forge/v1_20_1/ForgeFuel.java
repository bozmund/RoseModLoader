package rose.dialect.forge.v1_20_1;

import java.lang.reflect.Method;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

/**
 * Forge 1.20.1 items set their furnace burn time by overriding {@code IForgeItem.getBurnTime(ItemStack, RecipeType)}
 * (-1, the default, lets vanilla decide). A 26.3 furnace only burns items with a {@code cooking_fuel} component, so
 * an item that overrides it gets that component with the burn time it reports. The item is asked once, with an
 * empty stack: the component is fixed per item, and item stacks can't exist before components are built.
 */
final class ForgeFuel {
    /** Call after the item is registered. */
    static void register(Item item) {
        Method getBurnTime = burnTimeMethod(item);
        ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
        if (getBurnTime == null || key == null) return;
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(key, (components, context, k) -> {
            int burnTime = burnTime(item, getBurnTime);
            if (burnTime > 0) {
                components.set(DataComponents.COOKING_FUEL, new CookingFuel(new ResolvableInt.Constant(burnTime),
                        ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)));
            }
        });
    }

    /** The mod's override, or {@code null} if the item doesn't have one (26.3's Item has no such method). */
    private static Method burnTimeMethod(Item item) {
        try {
            return item.getClass().getMethod("getBurnTime", ItemStack.class, RecipeType.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static int burnTime(Item item, Method getBurnTime) {
        try {
            return (int) getBurnTime.invoke(item, ItemStack.EMPTY, RecipeType.SMELTING);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return -1;
        }
    }

    private ForgeFuel() {}
}
