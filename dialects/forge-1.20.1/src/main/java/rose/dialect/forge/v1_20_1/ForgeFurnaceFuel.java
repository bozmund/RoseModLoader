package rose.dialect.forge.v1_20_1;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;

/**
 * Forge 1.20.1 FurnaceFuelBurnTimeEvent, asked per stack where 26.3 checks fuel (as ForgeHooks.getBurnTime was): mods
 * can make any stack fuel, or change how long it burns.
 */
public final class ForgeFurnaceFuel {
    /** The burn time for {@code stack}: {@code vanilla} (from its cooking_fuel component) unless a listener sets one. */
    public static int burnTime(ItemStack stack, int vanilla, RecipeType<?> recipeType) {
        if (!ForgeDialect.active() || stack.isEmpty()) return vanilla;
        if (!((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(FurnaceFuelBurnTimeEvent.class)) return vanilla;
        FurnaceFuelBurnTimeEvent event = new FurnaceFuelBurnTimeEvent(stack, vanilla > 0 ? vanilla : -1, recipeType);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getBurnTime() < 0 ? vanilla : event.getBurnTime();
    }

    public static boolean isFuel(ItemStack stack, boolean vanilla, RecipeType<?> recipeType) {
        return vanilla || burnTime(stack, 0, recipeType) > 0;
    }

    private ForgeFurnaceFuel() {}
}
