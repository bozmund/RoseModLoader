package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.POS;
import static rose.testmods.addoncheck.AddonCheck.block;
import static rose.testmods.addoncheck.AddonCheck.craft;
import static rose.testmods.addoncheck.AddonCheck.item;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Comforts 6.4.0: sleeping bags and hammocks in every color, their Forge-tag recipes, and placing a two-block bag. */
final class ComfortsChecks {
    private static final String CF = "comforts";

    private ComfortsChecks() {}

    static void register() {
        test("cf_content_registered", ComfortsChecks::contentRegistered);
        test("cf_hammock_recipe_uses_forge_tags", ComfortsChecks::hammockRecipe);
        test("cf_sleeping_bags_can_be_dyed", ComfortsChecks::sleepingBagsCanBeDyed);
        test("cf_sleeping_bag_places_two_blocks", ComfortsChecks::sleepingBagPlacesTwoBlocks);
        test("cf_sleeping_bags_auto_place_only_at_night", ComfortsChecks::autoPlaceOnlyAtNight);
        test("cf_breaking_a_sleeping_bag_removes_both_halves", ComfortsChecks::breakingRemovesBothHalves);
    }

    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        for (DyeColor color : DyeColor.values()) {
            for (String kind : List.of("sleeping_bag_", "hammock_")) {
                String name = kind + color.getSerializedName();
                helper.assertTrue(block(CF, name).asItem() == item(CF, name), name + " isn't linked to its item");
            }
        }
        block(CF, "rope_and_nail");
        helper.succeed();
    }

    /** Wooden rods and string come from forge:rods/wooden and forge:string; the wool decides the color. */
    private static void hammockRecipe(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        ItemStack s = new ItemStack(Items.STRING);
        ItemStack w = new ItemStack(Items.STICK);
        ItemStack result = craft(helper, List.of(s, w, s, s, new ItemStack(Items.WOOL.black()), s, s, w, s));
        helper.assertTrue(result != null && result.is(item(CF, "hammock_black")), "string, sticks and black wool craft " + result);
        helper.succeed();
    }

    /** A white sleeping bag with a red dye (forge:dyes/red) becomes a red one. */
    private static void sleepingBagsCanBeDyed(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        grid.set(0, new ItemStack(item(CF, "sleeping_bag_white")));
        grid.set(1, new ItemStack(Items.DYE.red()));
        ItemStack result = craft(helper, grid);
        helper.assertTrue(result != null && result.is(item(CF, "sleeping_bag_red")), "dyeing a white sleeping bag red gives " + result);
        helper.succeed();
    }

    /**
     * Like a bed, a sleeping bag used on the ground places its foot and its head. Sneaking: otherwise Comforts' auto
     * use first checks that the player could sleep there now.
     */
    private static void sleepingBagPlacesTwoBlocks(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        helper.setBlock(POS, Blocks.STONE);
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel(); // Comforts tells the player why they can't sleep yet
        player.setShiftKeyDown(true);
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING); // isCrouching() follows the pose
        ItemStack bag = new ItemStack(item(CF, "sleeping_bag_blue"));
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        var abs = helper.absolutePos(POS);
        var result = player.gameMode.useItemOn(player, helper.getLevel(), bag, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false));
        var blue = block(CF, "sleeping_bag_blue");
        helper.assertTrue(helper.getBlockState(POS.above()).is(blue), "using the sleeping bag gave " + result + ", no bag placed");
        int halves = 0;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (helper.getBlockState(POS.above().relative(d)).is(blue)) halves++;
        }
        helper.assertTrue(halves == 1, "the sleeping bag has " + halves + " other halves next to it");
        helper.succeed();
    }

    /**
     * With auto use on, a sleeping bag used without sneaking is only placed if its user could sleep there now: Comforts'
     * trySleep reads the 1.20.1 day time, the dimension's natural flag and the BedSleepingProblem enum (all shimmed).
     */
    private static void autoPlaceOnlyAtNight(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        helper.setBlock(POS, Blocks.STONE);
        helper.setBlock(POS.east(3), Blocks.STONE);
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(POS.east().above())));
        var level = helper.getLevel();
        var overworld = level.registryAccess().getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD);
        long before = level.clockManager().getInstance(overworld).totalTicks();
        InteractionResult day;
        InteractionResult night;
        try {
            day = useAt(helper, player, POS, 6000L);
            night = useAt(helper, player, POS.east(3), 18000L);
        } finally {
            setTime(helper, before);
        }
        var bag = block(CF, "sleeping_bag_green");
        helper.assertTrue(!helper.getBlockState(POS.above()).is(bag), "a sleeping bag was placed at noon (" + day + ")");
        helper.assertTrue(helper.getBlockState(POS.east(3).above()).is(bag), "no sleeping bag placed at midnight (" + night + ")");
        helper.succeed();
    }

    private static InteractionResult useAt(GameTestHelper helper, ServerPlayer player, net.minecraft.core.BlockPos pos, long time) {
        setTime(helper, time);
        ItemStack bag = new ItemStack(item(CF, "sleeping_bag_green"));
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        var abs = helper.absolutePos(pos);
        return player.gameMode.useItemOn(player, helper.getLevel(), bag, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false));
    }

    /** Sets the overworld clock and the sky darkness that follows it (normally recomputed each tick). */
    private static void setTime(GameTestHelper helper, long ticks) {
        var level = helper.getLevel();
        level.clockManager().setTotalTicks(level.registryAccess().getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD), ticks);
        level.environmentAttributes().invalidateTickCache();
        level.updateSkyBrightness();
    }

    /** A player breaking one half runs Comforts' playerWillDestroy (a 1.20.1 void override); both halves go. */
    private static void breakingRemovesBothHalves(GameTestHelper helper) {
        if (skip(helper, CF)) return;
        helper.setBlock(POS, Blocks.STONE);
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(true);
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        ItemStack bag = new ItemStack(item(CF, "sleeping_bag_red"));
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        var abs = helper.absolutePos(POS);
        player.gameMode.useItemOn(player, helper.getLevel(), bag, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false));
        var red = block(CF, "sleeping_bag_red");
        helper.assertBlockPresent(red, POS.above());
        player.gameMode.destroyBlock(helper.absolutePos(POS.above()));
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(red, POS.above());
            for (Direction d : Direction.Plane.HORIZONTAL) helper.assertBlockNotPresent(red, POS.above().relative(d));
        });
    }
}
