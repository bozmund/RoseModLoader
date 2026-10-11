package rose.testmods.addoncheck;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import rose.api.ModInitializer;
import rose.api.gametest.RoseGameTests;
import rose.loader.RoseLoader;

/** Behavior checks for Forge 1.20.1 mods beyond Farmer's Delight. Each test passes as skipped when its mods are missing. */
public final class AddonCheck implements ModInitializer {
    static final BlockPos POS = new BlockPos(1, 1, 1);

    @Override
    public void onInitialize() {
        OceansDelightChecks.register();
        IronChestsChecks.register();
        CornDelightChecks.register();
        NaturesCompassChecks.register();
        ComfortsChecks.register();
        CuriosChecks.register();
        AppleSkinChecks.register();
        GeckoLibChecks.register();
    }

    /** The crafting recipe {@code grid} (3x3, row by row) matches, or null when none does. */
    static ItemStack craft(GameTestHelper helper, java.util.List<ItemStack> grid) {
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid);
        return helper.getLevel().recipeAccess()
                .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, helper.getLevel())
                .map(r -> r.value().assemble(input)).orElse(null);
    }

    static void test(String name, Consumer<GameTestHelper> test) {
        RoseGameTests.register(Identifier.fromNamespaceAndPath("addoncheck", name), test);
    }

    /** True (and the test passed as skipped) when one of {@code mods} isn't installed. */
    static boolean skip(GameTestHelper helper, String... mods) {
        for (String mod : mods) {
            if (RoseLoader.get().mod(mod).isEmpty()) {
                helper.succeed();
                return true;
            }
        }
        return false;
    }

    static Block block(String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalStateException("block missing: " + id);
        return BuiltInRegistries.BLOCK.getValue(id);
    }

    static Item item(String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalStateException("item missing: " + id);
        return BuiltInRegistries.ITEM.getValue(id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static BlockState with(BlockState state, String property, String value) {
        for (Property<?> p : state.getProperties()) {
            if (p.getName().equals(property)) {
                return state.setValue((Property) p, (Comparable) p.getValue(value).orElseThrow());
            }
        }
        throw new IllegalArgumentException(state + " has no " + property);
    }

    static Object call(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            return m.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + method + " on " + target, e);
        }
    }

    /** A class from an installed mod, initialized. */
    static Class<?> modClass(String name) {
        try {
            return Class.forName(name, true, RoseLoader.get().classLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("mod class missing: " + name, e);
        }
    }

    /** Calls the public static method {@code name} of {@code owner} whose parameter types are {@code types}. */
    static Object callStatic(Class<?> owner, String name, Class<?>[] types, Object... args) {
        try {
            return owner.getMethod(name, types).invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + owner.getName() + "." + name, e);
        }
    }

    static Object staticField(Class<?> owner, String name) {
        try {
            var field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't read " + owner.getName() + "." + name, e);
        }
    }

    static Object field(Object target, String name) {
        try {
            return target.getClass().getField(name).get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't read " + name + " of " + target, e);
        }
    }

    static ItemStack slot(Object handler, int slot) {
        try {
            return (ItemStack) handler.getClass().getMethod("getStackInSlot", int.class).invoke(handler, slot);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static void setSlot(Object handler, int slot, ItemStack stack) {
        try {
            handler.getClass().getMethod("setStackInSlot", int.class, ItemStack.class).invoke(handler, slot, stack);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
