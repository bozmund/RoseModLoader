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
        test("cu_block_experience_is_forges", CuriosChecks::blockExperienceIsForges);
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

    /**
     * Curios' block-break listener reads Forge's {@code BlockState.getExpDrop(level, random, pos, fortune, silkTouch)};
     * Rose answers with Forge 1.20.1's amounts.
     */
    private static void blockExperienceIsForges(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        Class<?> shim = modClass("rose.era.v1_20_1.shim.BlockStateShim");
        Class<?>[] types = {net.minecraft.world.level.block.state.BlockState.class, net.minecraft.world.level.LevelReader.class,
                net.minecraft.util.RandomSource.class, net.minecraft.core.BlockPos.class, int.class, int.class};
        var level = helper.getLevel();
        var random = net.minecraft.util.RandomSource.create(1);
        for (int i = 0; i < 20; i++) {
            int diamond = (int) callStatic(shim, "getExpDrop", types, net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState(), level, random, net.minecraft.core.BlockPos.ZERO, 0, 0);
            helper.assertTrue(diamond >= 3 && diamond <= 7, "diamond ore drops " + diamond + " experience");
            int redstone = (int) callStatic(shim, "getExpDrop", types, net.minecraft.world.level.block.Blocks.REDSTONE_ORE.defaultBlockState(), level, random, net.minecraft.core.BlockPos.ZERO, 0, 0);
            helper.assertTrue(redstone >= 1 && redstone <= 5, "redstone ore drops " + redstone + " experience");
        }
        int silk = (int) callStatic(shim, "getExpDrop", types, net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState(), level, random, net.minecraft.core.BlockPos.ZERO, 0, 1);
        int stone = (int) callStatic(shim, "getExpDrop", types, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), level, random, net.minecraft.core.BlockPos.ZERO, 0, 0);
        helper.assertTrue(silk == 0 && stone == 0, "silk touch gives " + silk + ", stone " + stone);
        helper.succeed();
    }
}
