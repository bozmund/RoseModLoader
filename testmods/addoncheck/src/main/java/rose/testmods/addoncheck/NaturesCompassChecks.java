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
}
