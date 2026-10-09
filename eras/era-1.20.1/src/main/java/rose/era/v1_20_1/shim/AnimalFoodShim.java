package rose.era.v1_20_1.shim;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.VillagerFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import rose.era.v1_20_1.ExtraTags;

/**
 * Redirect targets for 1.20.1's hard-coded animal and villager food sets. 26.3 replaced them with item tags
 * ({@code chicken_food}, {@code pig_food}, {@code cat_food}, {@code parrot_food}, {@code villager_picks_up}) and
 * the {@code villager_food} component. Reads see the tag; writes and additions are merged into it (ExtraTags) on the
 * next tag load. Removals have no effect.
 */
public final class AnimalFoodShim {
    public static Ingredient chickenFood() { return tagIngredient(ItemTags.CHICKEN_FOOD); }
    public static void setChickenFood(Ingredient value) { addIngredient(ItemTags.CHICKEN_FOOD, value); }
    public static Ingredient pigFood() { return tagIngredient(ItemTags.PIG_FOOD); }
    public static void setPigFood(Ingredient value) { addIngredient(ItemTags.PIG_FOOD, value); }
    public static Ingredient catFood() { return tagIngredient(ItemTags.CAT_FOOD); }
    public static void setCatFood(Ingredient value) { addIngredient(ItemTags.CAT_FOOD, value); }

    public static Set<Item> parrotFood() { return new TagSet(ItemTags.PARROT_FOOD); }
    public static void setParrotFood(Set<Item> value) { new TagSet(ItemTags.PARROT_FOOD).addAll(value); }
    public static Set<Item> villagerWantedItems() { return new TagSet(ItemTags.VILLAGER_PICKS_UP); }
    public static void setVillagerWantedItems(Set<Item> value) { new TagSet(ItemTags.VILLAGER_PICKS_UP).addAll(value); }

    /** 1.20.1 {@code Villager.FOOD_POINTS} (item -> food points); 26.3 has the {@code villager_food} component. */
    public static Map<Item, Integer> villagerFoodPoints() {
        return new FoodPoints();
    }

    public static void setVillagerFoodPoints(Map<Item, Integer> value) {
        new FoodPoints().putAll(value);
    }

    /** Before tags load the registry has no set for the tag yet; an unbound one still names it. */
    private static Ingredient tagIngredient(TagKey<Item> tag) {
        return Ingredient.of(BuiltInRegistries.ITEM.get(tag)
                .<HolderSet<Item>>map(set -> set).orElseGet(() -> HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
    }

    /** Adds what an ingredient matches to {@code tag}: its own holder set, or each child's (compound ingredients). */
    private static void addIngredient(TagKey<Item> tag, Ingredient ingredient) {
        List<?> children = children(ingredient);
        if (children == null) {
            ExtraTags.addAll(tag, ingredient.values);
            return;
        }
        for (Object child : children) {
            if (child instanceof Ingredient i) addIngredient(tag, i);
        }
    }

    /** {@code getChildren()} of a compound ingredient (Forge's CompoundIngredient, Rose's AnyOfIngredient), else null. */
    private static List<?> children(Ingredient ingredient) {
        for (Class<?> c = ingredient.getClass(); c != Ingredient.class; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod("getChildren");
                m.setAccessible(true);
                return (List<?>) m.invoke(ingredient);
            } catch (NoSuchMethodException e) {
                // keep looking in the superclass
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    /** A set view of an item tag: contents once tags are bound, additions go to ExtraTags. */
    private static final class TagSet extends AbstractSet<Item> {
        private final TagKey<Item> tag;

        TagSet(TagKey<Item> tag) {
            this.tag = tag;
        }

        private Set<Item> current() {
            try {
                return BuiltInRegistries.ITEM.get(tag).map(set -> set.stream().map(Holder::value).collect(Collectors.toSet())).orElse(Set.of());
            } catch (IllegalStateException unbound) {
                return Set.of(); // tags not bound yet
            }
        }

        @Override
        public boolean add(Item item) {
            BuiltInRegistries.ITEM.getResourceKey(item).ifPresent(k -> ExtraTags.addElement(tag, k.identifier()));
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof Item item && item.builtInRegistryHolder().is(tag);
        }

        @Override
        public Iterator<Item> iterator() {
            return current().iterator();
        }

        @Override
        public int size() {
            return current().size();
        }
    }

    private static final class FoodPoints extends AbstractMap<Item, Integer> {
        private static final Map<Item, Integer> ADDED = new HashMap<>();

        @Override
        public Integer put(Item item, Integer points) {
            ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
            if (key != null && points != null) {
                VillagerFood food = new VillagerFood(points);
                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(key, (components, context, k) -> components.set(DataComponents.VILLAGER_FOOD, food));
            }
            synchronized (ADDED) {
                return ADDED.put(item, points);
            }
        }

        @Override
        public Set<Entry<Item, Integer>> entrySet() {
            synchronized (ADDED) {
                return Map.copyOf(ADDED).entrySet();
            }
        }
    }

    private AnimalFoodShim() {}
}
