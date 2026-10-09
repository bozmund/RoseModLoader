package net.minecraft.world.item;

/** Era bridge (1.20.1): food that leaves a bowl. 26.3 expresses this with {@code usingConvertsTo(Items.BOWL)}. */
public class BowlFoodItem extends Item {
    public BowlFoodItem(Item.Properties properties) {
        super(properties.usingConvertsTo(Items.BOWL));
    }
}
