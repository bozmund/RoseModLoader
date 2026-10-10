package rose.era.v1_20_1.shim;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Redirect targets for 1.20.1 {@code FoodData} members. */
public final class FoodDataShim {
    /**
     * 1.20.1 {@code eat(item, stack)}: eat the item's food, if it has any. 26.3 {@code eat(FoodProperties)} adds the
     * same nutrition and saturation (1.20.1 passed nutrition and saturation modifier).
     */
    public static void eat(FoodData self, Item item, ItemStack stack) {
        FoodProperties food = item.components().get(DataComponents.FOOD);
        if (food != null) self.eat(food);
    }

    /** 1.20.1 {@code getExhaustionLevel()}; 26.3 keeps exhaustion private (widened by the era's access widener). */
    public static float getExhaustionLevel(FoodData self) {
        return self.exhaustionLevel;
    }

    /** 1.20.1 {@code setExhaustion(exhaustion)}. */
    public static void setExhaustion(FoodData self, float exhaustion) {
        self.exhaustionLevel = exhaustion;
    }

    private FoodDataShim() {}
}
