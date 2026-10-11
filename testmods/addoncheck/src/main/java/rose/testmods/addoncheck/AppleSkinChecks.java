package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.callStatic;
import static rose.testmods.addoncheck.AddonCheck.field;
import static rose.testmods.addoncheck.AddonCheck.modClass;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.staticField;
import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** AppleSkin 2.5.1: food values read through the 1.20.1 food API, and the server-side saturation sync. */
final class AppleSkinChecks {
    private static final String AS = "appleskin";

    private AppleSkinChecks() {}

    static void register() {
        test("as_reads_food_values", AppleSkinChecks::readsFoodValues);
        test("as_syncs_saturation_to_players", AppleSkinChecks::syncsSaturation);
    }

    /** FoodHelper reads nutrition and the 1.20.1 saturation modifier; hunger * modifier * 2 is 26.3's saturation. */
    private static void readsFoodValues(GameTestHelper helper) {
        if (skip(helper, AS)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bread = new ItemStack(Items.BREAD);
        Object values = callStatic(modClass("squeek.appleskin.helpers.FoodHelper"), "getDefaultFoodValues",
                new Class<?>[] {ItemStack.class, Player.class}, bread, player);
        var food = bread.get(DataComponents.FOOD);
        int hunger = (int) field(values, "hunger");
        float saturation = (float) AddonCheck.call(values, "getSaturationIncrement");
        helper.assertTrue(hunger == food.nutrition(), "bread restores " + hunger + " hunger, not " + food.nutrition());
        helper.assertTrue(Math.abs(saturation - food.saturation()) < 1.0E-4F, "bread gives " + saturation + " saturation, not " + food.saturation());
        helper.succeed();
    }

    /** SyncHandler's LivingTickEvent listener remembers the saturation it last sent to each server player. */
    @SuppressWarnings("unchecked")
    private static void syncsSaturation(GameTestHelper helper) {
        if (skip(helper, AS)) return;
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getFoodData().setSaturation(3.5F);
        Map<java.util.UUID, Float> sent = (Map<java.util.UUID, Float>) staticField(modClass("squeek.appleskin.network.SyncHandler"), "lastSaturationLevels");
        helper.succeedWhen(() -> {
            player.doTick();
            Float last = sent.get(player.getUUID());
            helper.assertTrue(last != null && last == 3.5F, "AppleSkin last sent saturation " + last);
        });
    }
}
