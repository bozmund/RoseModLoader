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
}
