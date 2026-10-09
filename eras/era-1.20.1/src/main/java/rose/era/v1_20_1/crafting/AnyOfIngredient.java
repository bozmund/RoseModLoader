package rose.era.v1_20_1.crafting;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient matching anything one of its parts matches: 1.20.1 ingredient lists could mix custom types with
 * items and tags (FD: "a knife tool action, or #forge:tools/knives"), which 26.3's HolderSet ingredients can't.
 * The superclass holds the first part's set for vanilla code that reads it directly.
 */
public class AnyOfIngredient extends Ingredient {
    private final List<Ingredient> parts;

    public AnyOfIngredient(List<Ingredient> parts) {
        super(parts.getFirst().values);
        this.parts = List.copyOf(parts);
    }

    public List<Ingredient> getChildren() {
        return parts;
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient part : parts) if (part.test(stack)) return true;
        return false;
    }

    @Override
    public boolean acceptsItem(Holder<Item> item) {
        for (Ingredient part : parts) if (part.acceptsItem(item)) return true;
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Stream<Holder<Item>> items() {
        return parts.stream().flatMap(Ingredient::items).distinct();
    }

    @Override
    public boolean isEmpty() {
        return parts.stream().allMatch(Ingredient::isEmpty);
    }
}
