package net.minecraftforge.common.crafting;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient matching anything one of its children matches. Children are asked lazily, so tag-based children
 * work even though tags are bound after this is created.
 *
 * <p>26.3's {@code Ingredient} is final over one {@code HolderSet}; Rose's access widener reopens it. The
 * superclass holds the first child's set (vanilla code that reads it directly, such as the network codec, sees
 * only that child).
 */
public class CompoundIngredient extends Ingredient {
    private final List<Ingredient> children;

    protected CompoundIngredient(List<Ingredient> children) {
        super(first(children).values);
        this.children = List.copyOf(children);
    }

    public static Ingredient of(Ingredient... children) {
        if (children.length == 1) return children[0];
        return new CompoundIngredient(Arrays.asList(children));
    }

    private static Ingredient first(List<Ingredient> children) {
        if (children.isEmpty()) throw new IllegalArgumentException("A compound ingredient needs at least one child");
        return children.getFirst();
    }

    public List<Ingredient> getChildren() {
        return children;
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (child.test(stack)) return true;
        }
        return false;
    }

    @Override
    public boolean acceptsItem(Holder<Item> item) {
        for (Ingredient child : children) {
            if (child.acceptsItem(item)) return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Stream<Holder<Item>> items() {
        return children.stream().flatMap(Ingredient::items).distinct();
    }

    @Override
    public boolean isEmpty() {
        return children.stream().allMatch(Ingredient::isEmpty);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CompoundIngredient other && children.equals(other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }
}
