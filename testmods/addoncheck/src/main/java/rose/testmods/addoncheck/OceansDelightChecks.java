package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.POS;
import static rose.testmods.addoncheck.AddonCheck.block;
import static rose.testmods.addoncheck.AddonCheck.call;
import static rose.testmods.addoncheck.AddonCheck.item;
import static rose.testmods.addoncheck.AddonCheck.setSlot;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.test;
import static rose.testmods.addoncheck.AddonCheck.with;

import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Ocean's Delight 1.0.2 (a Farmer's Delight add-on): its content, and FD's machines using its recipes. */
final class OceansDelightChecks {
    private static final String OD = "oceansdelight";
    private static final String FD = "farmersdelight";

    private OceansDelightChecks() {}

    static void register() {
        test("od_content_registered", OceansDelightChecks::contentRegistered);
        test("od_cutting_board_cuts_tentacles", OceansDelightChecks::cuttingBoardCutsTentacles);
        test("od_cooking_pot_braises_sea_pickles", OceansDelightChecks::cookingPotBraisesSeaPickles);
        test("od_squids_cut_with_a_knife_drop_tentacles", OceansDelightChecks::squidsDropTentacles);
        test("od_guardian_soup_is_served_in_bowls", OceansDelightChecks::guardianSoupIsServed);
    }

    private static Item od(String path) {
        return item(OD, path);
    }

    /** OD's items and its feast block exist; the creative tab that read CreativeModeTabs.SPAWN_EGGS was built. */
    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper, OD, FD)) return;
        for (String i : List.of("fugu_roll", "elder_guardian_roll", "squid_rings", "tentacles", "cut_tentacles", "guardian",
                "guardian_tail", "elder_guardian_slab", "elder_guardian_slice", "bowl_of_guardian_soup", "honey_fried_kelp")) {
            od(i);
        }
        Block soup = block(OD, "guardian_soup");
        helper.assertTrue(soup.asItem() == od("guardian_soup"), "guardian_soup block isn't linked to its item");
        helper.assertTrue(od("fugu_roll").components().has(net.minecraft.core.component.DataComponents.FOOD), "fugu_roll isn't food");
        helper.succeed();
    }

    /** OD's farmersdelight:cutting recipe: tentacles on a cutting board, cut with a knife, give three cut tentacles. */
    private static void cuttingBoardCutsTentacles(GameTestHelper helper) {
        if (skip(helper, OD, FD)) return;
        helper.setBlock(POS, block(FD, "cutting_board"));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(od("tentacles")));
        helper.useBlock(POS, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(FD, "iron_knife")));
        helper.useBlock(POS, player);
        helper.succeedWhen(() -> helper.assertItemEntityCountIs(od("cut_tentacles"), POS, 2.0, 3));
    }

    /** OD's farmersdelight:cooking recipe (wrapped in forge:conditional): a sea pickle and two brown mushrooms. */
    private static void cookingPotBraisesSeaPickles(GameTestHelper helper) {
        if (skip(helper, OD, FD)) return;
        helper.setBlock(POS, with(block(FD, "stove").defaultBlockState(), "lit", "true"));
        helper.setBlock(POS.above(), block(FD, "cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS.above(), BlockEntity.class);
        Object inventory = call(pot, "getInventory");
        setSlot(inventory, 0, new ItemStack(Items.SEA_PICKLE));
        setSlot(inventory, 1, new ItemStack(Items.BROWN_MUSHROOM));
        setSlot(inventory, 2, new ItemStack(Items.BROWN_MUSHROOM));
        helper.succeedWhen(() -> {
            ItemStack meal = (ItemStack) call(pot, "getMeal");
            helper.assertTrue(meal.is(od("braised_sea_pickle")), "no braised sea pickle yet, meal slot has " + meal);
        });
    }

    /** OD's global loot modifier cut_tentacles_squid: a squid killed by a player holding a knife also drops tentacles. */
    private static void squidsDropTentacles(GameTestHelper helper) {
        if (skip(helper, OD, FD)) return;
        var squid = helper.spawn(EntityTypes.SQUID, POS.above());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(FD, "iron_knife")));
        squid.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 100.0F);
        helper.succeedWhen(() -> helper.assertItemEntityPresent(od("tentacles"), POS.above(), 3.0));
    }

    /** Guardian soup is an FD feast: using it with a bowl hands out a bowl of guardian soup. */
    private static void guardianSoupIsServed(GameTestHelper helper) {
        if (skip(helper, OD, FD)) return;
        helper.setBlock(POS, block(OD, "guardian_soup"));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        helper.useBlock(POS, player);
        helper.assertTrue(player.getInventory().contains(new ItemStack(od("bowl_of_guardian_soup"))),
                "no bowl of guardian soup after serving; hand holds " + player.getMainHandItem());
        helper.succeed();
    }
}
