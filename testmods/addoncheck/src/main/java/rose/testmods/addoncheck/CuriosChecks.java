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
        test("cu_looting_level_event_fires", CuriosChecks::lootingLevelEventFires);
        test("cu_break_event_experience_drops", CuriosChecks::breakEventExperienceDrops);
        test("cu_fortune_mixin_applies", CuriosChecks::fortuneMixinApplies);
    }

    /** Positions whose BreakEvent experience the listener below sets to 42. */
    private static final java.util.Set<net.minecraft.core.BlockPos> GENEROUS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile boolean listeningToBreaks;

    /** Entities a LootingLevelEvent was fired for (the listener is added once, by the first test that needs it). */
    private static final java.util.Set<Object> LOOTED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static volatile boolean listening;

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

    /**
     * Curios adds curio looting through Forge's LootingLevelEvent, which Forge fired while a kill's loot was rolled.
     * A cow killed by a player rolls beef with an enchanted_count_increase (looting) function.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void lootingLevelEventFires(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        if (!listening) {
            listening = true;
            Object bus = AddonCheck.staticField(modClass("net.minecraftforge.common.MinecraftForge"), "EVENT_BUS");
            Class<?> priority = modClass("net.minecraftforge.eventbus.api.EventPriority");
            Class<?> event = modClass("net.minecraftforge.event.entity.living.LootingLevelEvent");
            java.util.function.Consumer<Object> listener = e -> LOOTED.add(call(e, "getEntity"));
            try {
                bus.getClass().getMethod("addListener", priority, boolean.class, Class.class, java.util.function.Consumer.class)
                        .invoke(bus, Enum.valueOf((Class) priority, "NORMAL"), false, event, listener);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        var cow = helper.spawn(net.minecraft.world.entity.EntityTypes.COW, net.minecraft.core.BlockPos.ZERO.above(2));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 100.0F);
        helper.succeedWhen(() -> helper.assertTrue(LOOTED.contains(cow), "no LootingLevelEvent for the killed cow"));
    }

    /** Curios changes a BreakEvent's experience (curio fortune); like Forge, the event's amount is what drops. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void breakEventExperienceDrops(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        if (!listeningToBreaks) {
            listeningToBreaks = true;
            Object bus = AddonCheck.staticField(modClass("net.minecraftforge.common.MinecraftForge"), "EVENT_BUS");
            Class<?> priority = modClass("net.minecraftforge.eventbus.api.EventPriority");
            Class<?> event = modClass("net.minecraftforge.event.level.BlockEvent$BreakEvent");
            java.util.function.Consumer<Object> listener = e -> {
                if (GENEROUS.contains(call(e, "getPos"))) {
                    try {
                        e.getClass().getMethod("setExpToDrop", int.class).invoke(e, 42);
                    } catch (ReflectiveOperationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            };
            try {
                bus.getClass().getMethod("addListener", priority, boolean.class, Class.class, java.util.function.Consumer.class)
                        .invoke(bus, Enum.valueOf((Class) priority, "NORMAL"), false, event, listener);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        var pos = net.minecraft.core.BlockPos.ZERO.above(1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.STONE);
        var abs = helper.absolutePos(pos);
        GENEROUS.add(abs);
        var player = (net.minecraft.server.level.ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        helper.assertTrue(player.gameMode.destroyBlock(abs), "the stone wasn't broken");
        helper.succeedWhen(() -> {
            int total = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, new net.minecraft.world.phys.AABB(abs).inflate(3))
                    .stream().mapToInt(net.minecraft.world.entity.ExperienceOrb::getValue).sum();
            helper.assertTrue(total == 42, "breaking dropped " + total + " experience, not the event's 42");
        });
    }

    /**
     * Curios' MixinApplyBonusCount adds curio fortune to fortune loot functions. Its @Shadow field changed type in 26.3
     * (Holder<Enchantment>); once rebased, the handler is part of ApplyBonusCount and fortune drops still roll.
     */
    private static void fortuneMixinApplies(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        boolean merged = java.util.Arrays.stream(net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().contains("applyFortune"));
        helper.assertTrue(merged, "Curios' fortune handler isn't in ApplyBonusCount");
        var pos = net.minecraft.core.BlockPos.ZERO.above(1);
        helper.setBlock(pos, net.minecraft.world.level.block.Blocks.DIAMOND_ORE);
        var player = (net.minecraft.server.level.ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pickaxe = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
        var fortune = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE);
        pickaxe.enchant(fortune, 3);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pickaxe);
        var abs = helper.absolutePos(pos);
        helper.assertTrue(player.gameMode.destroyBlock(abs), "the diamond ore wasn't broken");
        helper.succeedWhen(() -> {
            int diamonds = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(abs).inflate(3))
                    .stream().filter(e -> e.getItem().is(net.minecraft.world.item.Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum();
            helper.assertTrue(diamonds >= 1 && diamonds <= 4, "fortune III diamond ore dropped " + diamonds + " diamonds");
        });
    }
}
