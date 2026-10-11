package rose.era.v1_20_1.client;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import rose.era.v1_20_1.crafting.LegacyRecipeDisplay;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/**
 * Mods' recipe book tabs (Forge's RegisterRecipeBookCategoriesEvent) for the legacy recipe book. The server files
 * mod recipes under {@code rose:legacy}; here they are rebuilt from their source and sorted with the mod's own
 * category finder, as 1.20.1 clients sorted the recipes the server sent them.
 */
public final class LegacyRecipeBooks {
    /** A mod's tab in 26.3's recipe book. */
    public record Tab(LegacyRecipeBookCategory category) implements ExtendedRecipeBookCategory {}

    private static final Map<String, List<LegacyRecipeBookCategory>> BY_MOD = new ConcurrentHashMap<>();
    private static final Map<LegacyRecipeBookCategory, List<LegacyRecipeBookCategory>> AGGREGATES = new ConcurrentHashMap<>();
    private static final Map<RecipeType<?>, Function<Recipe<?>, LegacyRecipeBookCategory>> FINDERS = new ConcurrentHashMap<>();
    /** Recipes rebuilt on this client, by source JSON. */
    private static final Map<String, Optional<Recipe<?>>> REBUILT = new ConcurrentHashMap<>();

    private static volatile Runnable registration = () -> { };
    private static final java.util.concurrent.atomic.AtomicBoolean REGISTERED = new java.util.concurrent.atomic.AtomicBoolean();

    /**
     * How the dialect fills this in (Forge's RegisterRecipeBookCategoriesEvent). It runs on first use, in a world:
     * mods build their tab icons as item stacks, which 26.3 can't make before item components are bound.
     */
    public static void setRegistration(Runnable post) {
        registration = post;
    }

    private static void ensureRegistered() {
        if (REGISTERED.compareAndSet(false, true)) registration.run();
    }

    /** The tabs of {@code modId}'s recipe books, in order (Forge: per book type; mods name theirs after themselves). */
    public static void registerBookCategories(String modId, List<LegacyRecipeBookCategory> categories) {
        BY_MOD.computeIfAbsent(modId, m -> new ArrayList<>()).addAll(categories);
    }

    public static void registerAggregate(LegacyRecipeBookCategory aggregate, List<LegacyRecipeBookCategory> parts) {
        AGGREGATES.put(aggregate, List.copyOf(parts));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerFinder(RecipeType<?> type, Function<?, LegacyRecipeBookCategory> finder) {
        FINDERS.put(type, (Function) finder);
    }

    /** The tabs of a legacy menu's book: those the menu's mod registered. */
    public static List<RecipeBookComponent.TabInfo> tabsFor(RecipeBookMenu menu) {
        ensureRegistered();
        String mod;
        try {
            Identifier type = BuiltInRegistries.MENU.getKey(menu.getType());
            if (type == null) return List.of();
            mod = type.getNamespace();
        } catch (UnsupportedOperationException noType) {
            return List.of();
        }
        List<RecipeBookComponent.TabInfo> tabs = new ArrayList<>();
        for (LegacyRecipeBookCategory category : BY_MOD.getOrDefault(mod, List.of())) {
            List<ItemStack> icons = category.getIconItems();
            ItemStack primary = icons.isEmpty() ? ItemStack.EMPTY : icons.getFirst();
            Optional<ItemStack> secondary = icons.size() > 1 ? Optional.of(icons.get(1)) : Optional.empty();
            tabs.add(new RecipeBookComponent.TabInfo(primary, secondary, new Tab(category)));
        }
        return tabs;
    }

    /**
     * Adds the mods' tabs to the collections the client recipe book built: the {@code rose:legacy} collections (the
     * same objects, so the search box finds them) sorted by category, and the aggregate tabs over their parts.
     */
    public static Map<ExtendedRecipeBookCategory, List<RecipeCollection>> withModTabs(Map<ExtendedRecipeBookCategory, List<RecipeCollection>> byTab,
                                                                                     HolderLookup.Provider registries) {
        List<RecipeCollection> legacy = byTab.get(LegacyRecipeDisplay.CATEGORY);
        if (legacy == null || legacy.isEmpty()) return byTab;
        ensureRegistered();
        Map<LegacyRecipeBookCategory, List<RecipeCollection>> byCategory = new LinkedHashMap<>();
        for (RecipeCollection collection : legacy) {
            if (collection.getRecipes().isEmpty()) continue;
            LegacyRecipeBookCategory category = categoryOf(collection.getRecipes().getFirst().display(), registries);
            if (category != null) byCategory.computeIfAbsent(category, c -> new ArrayList<>()).add(collection);
        }
        Map<ExtendedRecipeBookCategory, List<RecipeCollection>> out = new HashMap<>(byTab);
        byCategory.forEach((category, collections) -> out.put(new Tab(category), List.copyOf(collections)));
        AGGREGATES.forEach((aggregate, parts) -> out.put(new Tab(aggregate),
                parts.stream().flatMap(p -> byCategory.getOrDefault(p, List.of()).stream()).toList()));
        return out;
    }

    /** The mod's category for a legacy recipe display (its finder on the rebuilt recipe), or {@code null}. */
    static LegacyRecipeBookCategory categoryOf(RecipeDisplay display, HolderLookup.Provider registries) {
        if (!(display instanceof LegacyRecipeDisplay legacy)) return null;
        Recipe<?> recipe = REBUILT.computeIfAbsent(legacy.json(), json -> Optional.ofNullable(
                LegacyRecipes.parse(legacy.serializer(), legacy.recipe(), JsonParser.parseString(json).getAsJsonObject(), registries))).orElse(null);
        if (recipe == null) return null;
        Function<Recipe<?>, LegacyRecipeBookCategory> finder = FINDERS.get(recipe.getType());
        if (finder == null) return null;
        try {
            return finder.apply(recipe);
        } catch (RuntimeException | LinkageError e) {
            LogUtils.getLogger().warn("[rose] recipe book category of {} failed: {}", legacy.recipe(), e.toString());
            return null;
        }
    }

    private LegacyRecipeBooks() {}
}
