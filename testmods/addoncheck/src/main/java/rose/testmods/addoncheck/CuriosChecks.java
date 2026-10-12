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
        test("cu_drop_rules_keep_their_overrides", CuriosChecks::dropRulesKeepTheirOverrides);
        test("cu_worn_curios_tick_and_drop", CuriosChecks::wornCuriosTickAndDrop);
        test("cu_curse_and_orb_shims", CuriosChecks::curseAndOrbShims);
        test("cu_curios_menu_opens", CuriosChecks::curiosMenuOpens);
        test("cu_equipped_curios_and_invalid_stacks", CuriosChecks::equippedCuriosAndInvalidStacks);
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

    /**
     * Curios' DropRulesEvent (fired when a wearer dies) keeps its overrides as 1.20.1 {@code net.minecraft.util.Tuple}s,
     * a class 26.3 no longer has; the era provides it.
     */
    private static void dropRulesKeepTheirOverrides(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Object handler = ((Optional<?>) call(callStatic(api(), "getCuriosInventory", new Class<?>[] {LivingEntity.class}, player), "resolve")).orElseThrow();
        Class<?> dropRule = modClass("top.theillusivec4.curios.api.type.capability.ICurio$DropRule");
        Class<?> event = modClass("top.theillusivec4.curios.api.event.DropRulesEvent");
        java.util.function.Predicate<net.minecraft.world.item.ItemStack> predicate = stack -> true;
        try {
            Object e = event.getConstructor(LivingEntity.class, modClass("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler"),
                    net.minecraft.world.damagesource.DamageSource.class, int.class, boolean.class)
                    .newInstance(player, handler, helper.getLevel().damageSources().generic(), 0, false);
            Object keep = dropRule.getField("ALWAYS_KEEP").get(null);
            event.getMethod("addOverride", java.util.function.Predicate.class, dropRule).invoke(e, predicate, keep);
            Object tuple = ((List<?>) event.getMethod("getOverrides").invoke(e)).get(0);
            helper.assertTrue(call(tuple, "getA") == predicate && call(tuple, "getB") == keep, "the override reads back as " + tuple);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        helper.succeed();
    }

    /**
     * A worn curio (Corn Delight's cob pipe, a head curio through Curios' item capability) is ticked with the wearer
     * (Curios calls 1.20.1 ItemStack.inventoryTick on it) and dropped when the wearer dies (Curios checks the 1.20.1
     * vanishing curse).
     */
    private static void wornCuriosTickAndDrop(GameTestHelper helper) {
        if (skip(helper, CU, "corn_delight")) return;
        // a player with a connection, as one that dies needs (placed at the world spawn, then moved here)
        var player = helper.makeMockServerPlayerInLevel();
        player.snapTo(helper.absoluteVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(net.minecraft.core.BlockPos.ZERO.above())));
        Object handler = ((Optional<?>) call(callStatic(api(), "getCuriosInventory", new Class<?>[] {LivingEntity.class}, player), "resolve")).orElseThrow();
        Object head = ((Optional<?>) AddonCheck.call2(handler, "getStacksHandler", String.class, "head")).orElseThrow(() -> new IllegalStateException("no head slot"));
        var pipe = new net.minecraft.world.item.ItemStack(AddonCheck.item("corn_delight", "cob_pipe"));
        AddonCheck.setSlot(call(head, "getStacks"), 0, pipe);
        // the LivingTickEvent Rose fires as a living entity ticks (a mock player has no connection to tick with)
        for (int i = 0; i < 3; i++) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(player));
        }
        helper.assertTrue(AddonCheck.slot(call(head, "getStacks"), 0).is(pipe.getItem()), "the worn pipe is gone after ticking");
        var where = player.getBoundingBox().inflate(4);
        player.die(helper.getLevel().damageSources().generic());
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                where, e -> e.getItem().is(pipe.getItem())).isEmpty(), "the worn pipe wasn't dropped"));
    }

    /**
     * Curios' drops and curio mending use 1.20.1 EnchantmentHelper.hasVanishingCurse/hasBindingCurse and the public
     * ExperienceOrb.value field: the curses are 26.3 enchantment effects, the value a private field with accessors.
     * Its slot modifiers are saved as 1.20.1 AttributeModifier tags.
     */
    private static void curseAndOrbShims(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var cursed = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET);
        cursed.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE), 1);
        var bound = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET);
        bound.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BINDING_CURSE), 1);
        helper.assertTrue(rose.era.v1_20_1.shim.EnchantmentShim.hasVanishingCurse(cursed) && !rose.era.v1_20_1.shim.EnchantmentShim.hasVanishingCurse(bound),
                "the vanishing curse isn't recognized");
        helper.assertTrue(rose.era.v1_20_1.shim.EnchantmentShim.hasBindingCurse(bound) && !rose.era.v1_20_1.shim.EnchantmentShim.hasBindingCurse(cursed),
                "the binding curse isn't recognized");
        var orb = new net.minecraft.world.entity.ExperienceOrb(helper.getLevel(), 0, 0, 0, 7);
        helper.assertTrue(rose.era.v1_20_1.shim.EntityShim.orbValue(orb) == 7, "the orb is worth " + rose.era.v1_20_1.shim.EntityShim.orbValue(orb));
        rose.era.v1_20_1.shim.EntityShim.setOrbValue(orb, 3);
        helper.assertTrue(orb.getValue() == 3, "the orb is worth " + orb.getValue() + " after setting 3");
        // curio slot modifiers are saved and loaded in 1.20.1's form, keeping their UUID
        var uuid = java.util.UUID.fromString("1f0ab2d2-3c43-4d5e-8f60-718293a4b5c6");
        var modifier = rose.era.v1_20_1.shim.AttributeModifierShim.create(uuid, "ring", 2.0,
                rose.era.v1_20_1.shim.AttributeModifierShim.fromValue(1));
        var loaded = rose.era.v1_20_1.shim.AttributeModifierShim.load(rose.era.v1_20_1.shim.AttributeModifierShim.save(modifier));
        helper.assertTrue(loaded != null && loaded.equals(modifier) && rose.era.v1_20_1.shim.AttributeModifierShim.getId(loaded).equals(uuid)
                && loaded.operation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                "the modifier came back as " + loaded);
        helper.succeed();
    }

    /**
     * The Curios button asks the server to open Curios' menu (CPacketOpenCurios), whose curio slots show their slot
     * type's icon through Forge's Slot.setBackground with 1.20.1 InventoryMenu.BLOCK_ATLAS.
     */
    private static void curiosMenuOpens(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        var player = helper.makeMockServerPlayerInLevel();
        try {
            var provider = (net.minecraft.world.MenuProvider) modClass("top.theillusivec4.curios.common.inventory.container.CuriosContainerProvider")
                    .getConstructor().newInstance();
            net.minecraftforge.network.NetworkHooks.openScreen(player, provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        var menu = player.containerMenu;
        helper.assertTrue(menu.getClass().getName().startsWith("top.theillusivec4.curios"), "the open menu is " + menu.getClass().getName());
        var backgrounds = menu.slots.stream().filter(slot -> slot.getClass().getSimpleName().equals("CurioSlot"))
                .map(slot -> ((rose.dialect.forge.v1_20_1.ForgeSlot) slot).rose$backgroundSprite()).toList();
        helper.assertTrue(!backgrounds.isEmpty() && backgrounds.stream().allMatch(java.util.Objects::nonNull), "curio slot backgrounds: " + backgrounds);
        helper.assertTrue(((rose.dialect.forge.v1_20_1.ForgeSlot) menu.slots.stream().filter(slot -> slot.getClass().getSimpleName().equals("CurioSlot"))
                .findFirst().orElseThrow()).rose$backgroundAtlas().toString().equals("minecraft:textures/atlas/blocks.png"), "the background atlas isn't the block atlas");
        // CurioSlot.getRenderStatus reads Forge's Slot.getSlotIndex(), the slot's index in its container
        var curioSlot = menu.slots.stream().filter(slot -> slot.getClass().getSimpleName().equals("CurioSlot")).findFirst().orElseThrow();
        helper.assertTrue((int) call(curioSlot, "getSlotIndex") == curioSlot.getContainerSlot(), "getSlotIndex isn't the container slot");
        call(curioSlot, "getRenderStatus");
        player.closeContainer();
        helper.succeed();
    }

    /**
     * Curios merges worn curios into a player's NBT (as /data get entity reads it) through getEquippedCurios, a Forge
     * CombinedInvWrapper over every slot type; curios a slot no longer accepts go back to the player through
     * ItemHandlerHelper.giveItemToPlayer.
     */
    private static void equippedCuriosAndInvalidStacks(GameTestHelper helper) {
        if (skip(helper, CU)) return;
        var player = helper.makeMockServerPlayerInLevel();
        Object handler = ((Optional<?>) call(callStatic(api(), "getCuriosInventory", new Class<?>[] {LivingEntity.class}, player), "resolve")).orElseThrow();
        Object head = ((Optional<?>) AddonCheck.call2(handler, "getStacksHandler", String.class, "head")).orElseThrow(() -> new IllegalStateException("no head slot"));
        var apple = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE);
        AddonCheck.setSlot(call(head, "getStacks"), 0, apple);
        var equipped = (net.minecraftforge.items.IItemHandler) call(handler, "getEquippedCurios");
        boolean found = false;
        for (int i = 0; i < equipped.getSlots(); i++) found |= equipped.getStackInSlot(i).is(net.minecraft.world.item.Items.APPLE);
        helper.assertTrue(found, "the worn apple isn't among the " + equipped.getSlots() + " equipped curios");
        net.minecraft.advancements.predicates.NbtPredicate.getEntityTagToCompare(player);

        AddonCheck.call2(handler, "loseInvalidStack", net.minecraft.world.item.ItemStack.class, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK, 3));
        call(handler, "handleInvalidStacks");
        helper.assertTrue(player.getInventory().getNonEquipmentItems().stream().filter(s -> s.is(net.minecraft.world.item.Items.STICK)).mapToInt(net.minecraft.world.item.ItemStack::getCount).sum() == 3, "the invalid stack wasn't given back to the player");
        helper.succeed();
    }
}
