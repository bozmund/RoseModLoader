package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.call;
import static rose.testmods.addoncheck.AddonCheck.callStatic;
import static rose.testmods.addoncheck.AddonCheck.modClass;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/** Curios API 5.14.1: slot types loaded from its data pack, and the curios inventory capability on players. */
final class CuriosChecks {
    private static final String CU = "curios";

    private CuriosChecks() {}

    static void register() {
        test("cu_slot_types_loaded", CuriosChecks::slotTypesLoaded);
        test("cu_players_have_a_curios_inventory", CuriosChecks::playersHaveAnInventory);
    }

    private static Class<?> api() {
        return modClass("top.theillusivec4.curios.api.CuriosApi");
    }

    /** data/curios/curios/slots/*.json became slot types on the server. */
    private static void slotTypesLoaded(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        Map<?, ?> slots = (Map<?, ?>) callStatic(api(), "getSlots", new Class<?>[] {boolean.class}, false);
        for (String slot : List.of("back", "belt", "body", "bracelet", "charm", "curio", "hands", "head", "necklace", "ring")) {
            helper.assertTrue(slots.containsKey(slot), "no " + slot + " slot type; loaded " + slots.keySet());
        }
        helper.succeed();
    }

    /** Curios attaches its inventory to players through AttachCapabilitiesEvent; the handler knows its wearer. */
    private static void playersHaveAnInventory(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Object lazy = callStatic(api(), "getCuriosInventory", new Class<?>[] {LivingEntity.class}, player);
        Optional<?> handler = (Optional<?>) call(lazy, "resolve");
        helper.assertTrue(handler.isPresent(), "the player has no curios inventory");
        helper.assertTrue(call(handler.get(), "getWearer") == player, "the curios inventory belongs to someone else");
        helper.succeed();
    }
}
