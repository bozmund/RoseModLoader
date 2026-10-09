package rose.testmods.fdcheck;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import rose.api.ModInitializer;
import rose.api.gametest.RoseGameTests;
import rose.loader.RoseLoader;

/**
 * Behavior checks for Farmer's Delight 1.20.1 running through Rose's Forge dialect and era bridge. Each test
 * passes as skipped when FD isn't installed.
 */
public final class FdCheck implements ModInitializer {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @Override
    public void onInitialize() {
        RoseGameTests.register(id("content_registered"), FdCheck::contentRegistered);
        RoseGameTests.register(id("cutting_board_cuts_cabbage"), FdCheck::cuttingBoardCutsCabbage);
        RoseGameTests.register(id("cooking_pot_cooks_beef_stew"), FdCheck::cookingPotCooksBeefStew);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("fdcheck", path);
    }

    private static boolean skip(GameTestHelper helper) {
        if (RoseLoader.get().mod("farmersdelight").isPresent()) return false;
        helper.succeed(); // Farmer's Delight not installed
        return true;
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farmersdelight", path));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("farmersdelight", path));
    }

    /** FD's main blocks and items exist (each was created by FD's own registration code). */
    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String b : List.of("stove", "cooking_pot", "cutting_board", "skillet", "bamboo_basket", "cabbages", "rice", "budding_tomatoes",
                "tomatoes", "rope", "rich_soil", "rich_soil_farmland", "organic_compost", "wild_cabbages", "rope_fence_gate")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("farmersdelight", b)), "block missing: " + b);
        }
        for (String i : List.of("cabbage", "tomato", "onion", "rice", "iron_knife", "flint_knife", "beef_stew", "cabbage_leaf",
                "tomato_seeds", "skillet", "cooking_pot", "hamburger")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("farmersdelight", i)), "item missing: " + i);
        }
        helper.succeed();
    }

    /** Place a cabbage on a cutting board (right-click), then cut it with a knife: two cabbage leaves drop. */
    private static void cuttingBoardCutsCabbage(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("cutting_board"));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("cabbage")));
        helper.useBlock(POS, player);
        BlockEntity board = helper.getBlockEntity(POS, BlockEntity.class);
        ItemStack stored = (ItemStack) call(board, "getStoredItem");
        helper.assertTrue(stored.is(item("cabbage")), "board should hold the cabbage, holds " + stored);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("iron_knife")));
        helper.useBlock(POS, player);
        helper.succeedWhen(() -> helper.assertItemEntityCountIs(item("cabbage_leaf"), POS, 2.0, 2));
    }

    /** A cooking pot on a lit stove turns beef, a carrot and a potato into beef stew. */
    private static void cookingPotCooksBeefStew(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockState stove = block("stove").defaultBlockState();
        for (Property<?> p : stove.getProperties()) {
            if (p.getName().equals("lit") && p instanceof BooleanProperty lit) stove = stove.setValue(lit, true);
        }
        helper.setBlock(POS, stove);
        helper.setBlock(POS.above(), block("cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS.above(), BlockEntity.class);
        Object inventory = call(pot, "getInventory");
        setSlot(inventory, 0, new ItemStack(net.minecraft.world.item.Items.BEEF));
        setSlot(inventory, 1, new ItemStack(net.minecraft.world.item.Items.CARROT));
        setSlot(inventory, 2, new ItemStack(net.minecraft.world.item.Items.POTATO));
        helper.succeedWhen(() -> {
            ItemStack meal = (ItemStack) call(pot, "getMeal");
            helper.assertTrue(meal.is(item("beef_stew")), "no beef stew yet, meal slot has " + meal);
        });
    }

    private static void setSlot(Object handler, int slot, ItemStack stack) {
        try {
            Method set = handler.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            set.invoke(handler, slot, stack);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object call(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            return m.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + method + " on " + target, e);
        }
    }
}
