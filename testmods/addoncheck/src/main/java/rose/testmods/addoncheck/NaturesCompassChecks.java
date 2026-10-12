package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.craft;
import static rose.testmods.addoncheck.AddonCheck.item;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Nature's Compass 1.12.0: the compass item and its recipe, whose tags have no namespace ("saplings", "logs"). */
final class NaturesCompassChecks {
    private static final String NC = "naturescompass";

    private NaturesCompassChecks() {}

    static void register() {
        test("nc_compass_registered", NaturesCompassChecks::compassRegistered);
        test("nc_compass_recipe_crafts", NaturesCompassChecks::compassRecipe);
        test("nc_compass_finds_a_biome", NaturesCompassChecks::compassFindsABiome);
        test("nc_compass_points_somewhere_before_a_search", NaturesCompassChecks::compassAngle);
        test("nc_compass_knows_its_frame", NaturesCompassChecks::compassKnowsItsFrame);
        test("nc_op_check_reads_the_op_list", NaturesCompassChecks::opCheck);
    }

    private static void compassRegistered(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        ItemStack compass = new ItemStack(item(NC, "naturescompass"));
        helper.assertTrue(compass.getMaxStackSize() == 1, "nature's compass stacks to " + compass.getMaxStackSize());
        helper.succeed();
    }

    private static void compassRecipe(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        ItemStack s = new ItemStack(Items.OAK_SAPLING);
        ItemStack l = new ItemStack(Items.BIRCH_LOG);
        ItemStack result = craft(helper, List.of(s, l, s, l, new ItemStack(Items.COMPASS), l, s, l, s));
        helper.assertTrue(result != null && result.is(item(NC, "naturescompass")), "saplings, logs and a compass craft " + result);
        helper.succeed();
    }

    /**
     * A search for the biome the player stands in: NC's BiomeSearchWorker runs as a Forge world worker, samples the
     * biome source with the 1.20.1 noise API, and sets the compass to FOUND.
     */
    private static void compassFindsABiome(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        var level = helper.getLevel();
        var pos = helper.absolutePos(net.minecraft.core.BlockPos.ZERO.above(2));
        var biome = level.getBiome(pos).unwrapKey().orElseThrow().identifier();
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack stack = new ItemStack(item(NC, "naturescompass"));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        Object compass = stack.getItem();
        try {
            compass.getClass().getMethod("searchForBiome", net.minecraft.server.level.ServerLevel.class, net.minecraft.world.entity.player.Player.class,
                    net.minecraft.resources.Identifier.class, net.minecraft.core.BlockPos.class, ItemStack.class)
                    .invoke(compass, level, player, biome, pos, stack);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        helper.succeedWhen(() -> {
            Object state = AddonCheck.call2(compass, "getState", ItemStack.class, stack);
            helper.assertTrue(String.valueOf(state).equals("FOUND"), "the compass is " + state + " looking for " + biome);
        });
    }

    /**
     * Before a search the compass needle points at the world spawn, which NC reads with 1.20.1
     * {@code getSharedSpawnPos} (26.3: getRespawnData).
     */
    private static void compassAngle(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        var level = helper.getLevel();
        Object pos = AddonCheck.callStatic(AddonCheck.modClass("rose.era.v1_20_1.shim.LevelShim"), "getSharedSpawnPos",
                new Class<?>[] {net.minecraft.world.level.Level.class}, level);
        helper.assertTrue(level.getRespawnData().pos().equals(pos), "the 1.20.1 spawn position is " + pos);
        helper.succeed();
    }

    /**
     * NC's needle turns with the item frame showing it (1.20.1 {@code isFramed}/{@code getFrame}); 26.3 stacks don't
     * know their frame, so Rose answers from the frame its item property is read for.
     */
    @SuppressWarnings("unchecked")
    private static void compassKnowsItsFrame(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        Class<?> shim = AddonCheck.modClass("rose.era.v1_20_1.shim.ItemStackShim");
        ItemStack stack = new ItemStack(item(NC, "naturescompass"));
        var frame = helper.spawn(net.minecraft.world.entity.EntityTypes.ITEM_FRAME, net.minecraft.core.BlockPos.ZERO.above(2));
        java.util.function.Supplier<Float> framed = () -> (boolean) AddonCheck.callStatic(shim, "isFramed", new Class<?>[] {ItemStack.class}, stack) ? 1F : 0F;
        Class<?>[] types = {net.minecraft.world.entity.Entity.class, java.util.function.Supplier.class};
        helper.assertTrue((float) AddonCheck.callStatic(shim, "withPropertyOwner", types, frame, framed) == 1F, "a stack in a frame isn't framed");
        helper.assertTrue((float) AddonCheck.callStatic(shim, "withPropertyOwner", types, null, framed) == 0F, "a stack outside a frame is framed");
        Object got = AddonCheck.callStatic(shim, "withPropertyOwner", types, frame, (java.util.function.Supplier<Float>)
                () -> AddonCheck.callStatic(shim, "getFrame", new Class<?>[] {ItemStack.class}, stack) == frame ? 1F : 0F);
        helper.assertTrue((float) got == 1F, "getFrame isn't the frame");
        helper.succeed();
    }

    /**
     * Every compass use on a dedicated server asks NC's PlayerUtils.isOp, which looks the player's 1.20.1 GameProfile
     * up in the op list; 26.3 keys that list by NameAndId.
     */
    private static void opCheck(GameTestHelper helper) {
        if (skip(helper, NC)) return;
        var player = helper.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Class<?> utils = AddonCheck.modClass("com.chaosthedude.naturescompass.util.PlayerUtils");
        Class<?>[] types = {net.minecraft.world.entity.player.Player.class};
        helper.assertTrue(!(boolean) AddonCheck.callStatic(utils, "isOp", types, player), "a new player is an op");
        var players = helper.getLevel().getServer().getPlayerList();
        var id = new net.minecraft.server.players.NameAndId(player.getGameProfile());
        players.op(id);
        try {
            helper.assertTrue((boolean) AddonCheck.callStatic(utils, "isOp", types, player), "an op isn't an op");
        } finally {
            players.deop(id);
        }
        helper.succeed();
    }
}
