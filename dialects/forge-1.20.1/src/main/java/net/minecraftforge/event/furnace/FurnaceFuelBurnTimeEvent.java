package net.minecraftforge.event.furnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** How long a stack burns as furnace fuel (forge bus). -1 keeps the item's own value; setting a time cancels the event. */
@Cancelable
public class FurnaceFuelBurnTimeEvent extends Event {
    private final ItemStack itemStack;
    private final RecipeType<?> recipeType;
    private int burnTime;

    public FurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime, RecipeType<?> recipeType) {
        this.itemStack = itemStack;
        this.burnTime = burnTime;
        this.recipeType = recipeType;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public RecipeType<?> getRecipeType() {
        return recipeType;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        if (burnTime >= 0) {
            this.burnTime = burnTime;
            setCanceled(true);
        }
    }
}
