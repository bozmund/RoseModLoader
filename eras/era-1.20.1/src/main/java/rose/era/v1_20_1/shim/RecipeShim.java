package rose.era.v1_20_1.shim;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import rose.era.v1_20_1.EraContext;
import rose.era.v1_20_1.bridge.Legacy;
import rose.era.v1_20_1.mixin.RecipeManagerAccess;

/**
 * Redirect targets for 1.20.1 recipe lookups. 26.3 keeps recipes on the server only, hands out RecipeHolders
 * (recipe + id) and matches RecipeInputs; 1.20.1 code wants the recipe objects, keyed by id, matched against
 * Containers.
 */
public final class RecipeShim {
    /** Ids of recipes handed to old code (1.20.1 recipes knew their own id). */
    private static final Map<Recipe<?>, Identifier> IDS = Collections.synchronizedMap(new WeakHashMap<>());

    /** 1.20.1 {@code level.getRecipeManager()}: the server's recipes (on a client, the integrated server's). */
    public static RecipeManager getRecipeManager(Level level) {
        if (level instanceof ServerLevel server) return server.recipeAccess();
        MinecraftServer server = EraContext.server();
        if (server != null) return server.getRecipeManager();
        throw new IllegalStateException("Recipes live on the server in 26.3; this client has no access to them");
    }

    public static <T extends Recipe<?>> Optional<T> getRecipeFor(RecipeManager manager, RecipeType<T> type, Container container, Level level) {
        for (RecipeHolder<T> holder : byTypeHolders(manager, type)) {
            if (matches(holder.value(), container, level)) return Optional.of(remember(holder));
        }
        return Optional.empty();
    }

    public static <T extends Recipe<?>> List<T> getRecipesFor(RecipeManager manager, RecipeType<T> type, Container container, Level level) {
        List<T> out = new ArrayList<>();
        for (RecipeHolder<T> holder : byTypeHolders(manager, type)) {
            if (matches(holder.value(), container, level)) out.add(remember(holder));
        }
        return out;
    }

    public static <T extends Recipe<?>> List<T> getAllRecipesFor(RecipeManager manager, RecipeType<T> type) {
        List<T> out = new ArrayList<>();
        for (RecipeHolder<T> holder : byTypeHolders(manager, type)) out.add(remember(holder));
        return out;
    }

    public static Optional<? extends Recipe<?>> byKey(RecipeManager manager, Identifier id) {
        return manager.byKey(ResourceKey.create(Registries.RECIPE, id)).map(RecipeShim::remember);
    }

    /** {@code @Invoker("byType")}: the recipes of one type by id. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<Identifier, Recipe<?>> byType(RecipeManager manager, RecipeType<?> type) {
        Map<Identifier, Recipe<?>> out = new LinkedHashMap<>();
        for (Object o : byTypeHolders(manager, (RecipeType) type)) {
            RecipeHolder<?> holder = (RecipeHolder<?>) o;
            out.put(holder.id().identifier(), remember(holder));
        }
        return out;
    }

    /** 1.20.1 {@code RecipeManager.CachedCheck.getRecipeFor(container, level)}. */
    public static <T extends Recipe<?>> Optional<T> cachedGetRecipeFor(RecipeManager.CachedCheck<?, T> check, Container container, Level level) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Optional<RecipeHolder<T>> found = ((RecipeManager.CachedCheck) check).getRecipeFor(inputFor(container), (ServerLevel) level);
        return found.map(RecipeShim::remember);
    }

    /** 1.20.1 {@code recipe.getId()}: mod recipes know their id; vanilla ones are looked up. */
    public static Identifier getId(Recipe<?> recipe) {
        var own = Legacy.find(recipe, "getId", MethodType.methodType(Identifier.class));
        if (own.isPresent()) return (Identifier) Legacy.invoke(own.get(), recipe);
        return IDS.get(recipe);
    }

    /** 1.20.1 {@code recipe.getResultItem(registryAccess)}. */
    public static ItemStack getResultItem(Recipe<?> recipe, RegistryAccess access) {
        var own = Legacy.find(recipe, "getResultItem", MethodType.methodType(ItemStack.class, RegistryAccess.class));
        if (own.isPresent()) return (ItemStack) Legacy.invoke(own.get(), recipe, access);
        if (recipe instanceof SingleItemRecipe single) return single.result().create();
        return ItemStack.EMPTY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Recipe<?>> java.util.Collection<RecipeHolder<T>> byTypeHolders(RecipeManager manager, RecipeType<T> type) {
        return (java.util.Collection) ((RecipeManagerAccess) manager).rose$recipes().byType((RecipeType) type);
    }

    private static <T extends Recipe<?>> T remember(RecipeHolder<T> holder) {
        IDS.put(holder.value(), holder.id().identifier());
        return holder.value();
    }

    /** 1.20.1 {@code recipe.matches(container, level)}; also the redirect target for that call. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean matches(Recipe<?> recipe, Container container, Level level) {
        var own = Legacy.find(recipe, "matches", MethodType.methodType(boolean.class, Container.class, Level.class));
        if (own.isPresent()) return (Boolean) Legacy.invoke(own.get(), recipe, container, level);
        try {
            return ((Recipe) recipe).matches(inputFor(container), level);
        } catch (ClassCastException wrongInputType) {
            return false;
        }
    }

    /** 1.20.1 {@code recipe.assemble(container, registryAccess)}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ItemStack assemble(Recipe<?> recipe, Container container, RegistryAccess access) {
        var own = Legacy.find(recipe, "assemble", MethodType.methodType(ItemStack.class, Container.class, RegistryAccess.class));
        if (own.isPresent()) return (ItemStack) Legacy.invoke(own.get(), recipe, container, access);
        return ((Recipe) recipe).assemble(inputFor(container));
    }

    /** The 26.3 input for an old container: crafting grids, single-slot inputs, or the container itself. */
    static RecipeInput inputFor(Container container) {
        if (container instanceof RecipeInput input) return input;
        if (container instanceof CraftingContainer grid) return CraftingInput.of(grid.getWidth(), grid.getHeight(), grid.getItems());
        if (container.getContainerSize() == 1) return new SingleRecipeInput(container.getItem(0));
        return rose.era.v1_20_1.bridge.LegacyContainers.asInput(container);
    }

    private RecipeShim() {}
}
