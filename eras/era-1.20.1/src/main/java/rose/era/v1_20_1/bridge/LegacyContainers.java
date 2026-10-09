package rose.era.v1_20_1.bridge;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** 1.20.1 recipes matched Containers; 26.3 passes RecipeInputs. */
public final class LegacyContainers {
    public static Container asContainer(RecipeInput input) {
        if (input instanceof Container container) return container;
        if (input instanceof net.minecraft.world.item.crafting.CraftingInput crafting) return new LegacyCraftingContainer(crafting);
        SimpleContainer container = new SimpleContainer(input.size());
        for (int i = 0; i < input.size(); i++) container.setItem(i, input.getItem(i));
        return container;
    }

    /** A Container's items as a RecipeInput (for 26.3 code given a 1.20.1 container). */
    public static RecipeInput asInput(Container container) {
        if (container instanceof RecipeInput input) return input;
        return new RecipeInput() {
            @Override
            public ItemStack getItem(int index) {
                return container.getItem(index);
            }

            @Override
            public int size() {
                return container.getContainerSize();
            }
        };
    }

    private LegacyContainers() {}
}
