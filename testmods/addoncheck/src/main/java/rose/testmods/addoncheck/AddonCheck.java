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
        test("creative_tabs_have_places", AddonCheck::creativeTabsHavePlaces);
        test("legacy_key_codes_are_26_3_keys", AddonCheck::legacyKeyCodes);
    }

    /**
     * 1.20.1 mods name keys by GLFW key code (AppleSkin and Curios check shift with isKeyDown(window, 340)); 26.3 key
     * codes are SDL scancodes, and mouse buttons count from 1.
     */
    private static void legacyKeyCodes(GameTestHelper helper) {
        Class<?> codes = modClass("rose.era.v1_20_1.client.LegacyKeyCodes");
        int[][] keys = {{340, 225}, {344, 229}, {341, 224}, {65, 4}, {48, 39}, {256, 41}, {290, 58}, {32, 44}, {-1, 0}};
        for (int[] k : keys) {
            int got = (int) callStatic(codes, "key", new Class<?>[] {int.class}, k[0]);
            helper.assertTrue(got == k[1], "GLFW key " + k[0] + " became " + got + ", not " + k[1]);
        }
        int[][] buttons = {{0, 1}, {1, 3}, {2, 2}, {3, 4}};
        for (int[] b : buttons) {
            int got = (int) callStatic(codes, "mouseButton", new Class<?>[] {int.class}, b[0]);
            helper.assertTrue(got == b[1], "GLFW mouse button " + b[0] + " became " + got + ", not " + b[1]);
        }
        helper.succeed();
    }

    /**
     * Every creative tab sits in one of vanilla's seven places per row, or (Forge mods' tabs) at column 100 and up,
     * which Rose shows on pages of their own.
     */
    private static void creativeTabsHavePlaces(GameTestHelper helper) {
        java.util.List<String> misplaced = new java.util.ArrayList<>();
        for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (tab.column() > 6 && tab.column() < 100) misplaced.add(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab) + "@" + tab.row() + "/" + tab.column());
        }
        helper.assertTrue(misplaced.isEmpty(), "creative tabs off the screen: " + misplaced);
        helper.succeed();
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

    /** Calls the public one-argument method {@code method} of {@code target}. */
    static Object call2(Object target, String method, Class<?> type, Object arg) {
        try {
            return target.getClass().getMethod(method, type).invoke(target, arg);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + method + " on " + target, e);
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
