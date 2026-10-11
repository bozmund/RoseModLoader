package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.POS;
import static rose.testmods.addoncheck.AddonCheck.block;
import static rose.testmods.addoncheck.AddonCheck.item;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Iron Chests 14.4.4: bigger chests, their saved contents, and the upgrade items. */
final class IronChestsChecks {
    private static final String IC = "ironchest";

    private IronChestsChecks() {}

    static void register() {
        test("ic_content_registered", IronChestsChecks::contentRegistered);
        test("ic_chests_have_their_size", IronChestsChecks::chestsHaveTheirSize);
        test("ic_chests_keep_items_when_reloaded", IronChestsChecks::chestsKeepItemsWhenReloaded);
        test("ic_upgrade_turns_a_chest_into_an_iron_chest", IronChestsChecks::upgradeKeepsItems);
        test("ic_chests_open_a_menu", IronChestsChecks::chestsOpenAMenu);
    }

    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper, IC)) return;
        for (String chest : List.of("iron", "gold", "diamond", "copper", "crystal", "obsidian", "dirt")) {
            helper.assertTrue(block(IC, chest + "_chest").asItem() == item(IC, chest + "_chest"), chest + "_chest isn't linked to its item");
            block(IC, "trapped_" + chest + "_chest");
        }
        for (String upgrade : List.of("wood_to_iron", "iron_to_gold", "gold_to_diamond", "copper_to_iron", "diamond_to_crystal")) {
            item(IC, upgrade + "_chest_upgrade");
        }
        helper.succeed();
    }

    /** Each tier's block entity is a container of its tier's size (iron 54, gold 81, diamond 108, copper 45, dirt 1). */
    private static void chestsHaveTheirSize(GameTestHelper helper) {
        if (skip(helper, IC)) return;
        int[][] sizes = {{0, 54}, {1, 81}, {2, 108}, {3, 45}, {4, 1}};
        String[] names = {"iron", "gold", "diamond", "copper", "dirt"};
        for (int[] s : sizes) {
            var pos = POS.east(s[0] * 2);
            helper.setBlock(pos, block(IC, names[s[0]] + "_chest"));
            Container chest = (Container) helper.getBlockEntity(pos, BlockEntity.class);
            helper.assertTrue(chest.getContainerSize() == s[1], names[s[0]] + " chest holds " + chest.getContainerSize() + ", not " + s[1]);
        }
        helper.succeed();
    }

    /** What an iron chest holds survives saving and loading its block entity. */
    private static void chestsKeepItemsWhenReloaded(GameTestHelper helper) {
        if (skip(helper, IC)) return;
        helper.setBlock(POS, block(IC, "iron_chest"));
        BlockEntity chest = helper.getBlockEntity(POS, BlockEntity.class);
        ((Container) chest).setItem(0, new ItemStack(Items.DIAMOND, 7));
        ((Container) chest).setItem(53, new ItemStack(Items.GOLDEN_APPLE, 2));
        var registries = helper.getLevel().registryAccess();
        BlockEntity reloaded = BlockEntity.loadStatic(chest.getBlockPos(), chest.getBlockState(), chest.saveWithFullMetadata(registries), registries);
        helper.assertTrue(reloaded instanceof Container, "the iron chest didn't load back");
        Container c = (Container) reloaded;
        helper.assertTrue(c.getItem(0).is(Items.DIAMOND) && c.getItem(0).getCount() == 7, "slot 0 holds " + c.getItem(0));
        helper.assertTrue(c.getItem(53).is(Items.GOLDEN_APPLE) && c.getItem(53).getCount() == 2, "slot 53 holds " + c.getItem(53));
        helper.succeed();
    }

    /**
     * A wood-to-iron upgrade used on a vanilla chest (through the player's game mode, so Forge's onItemUseFirst runs)
     * replaces it with an iron chest that keeps its items.
     */
    private static void upgradeKeepsItems(GameTestHelper helper) {
        if (skip(helper, IC)) return;
        helper.setBlock(POS, Blocks.CHEST);
        ((Container) helper.getBlockEntity(POS, BlockEntity.class)).setItem(3, new ItemStack(Items.EMERALD, 5));
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        ItemStack upgrade = new ItemStack(item(IC, "wood_to_iron_chest_upgrade"));
        player.setItemInHand(InteractionHand.MAIN_HAND, upgrade);
        var abs = helper.absolutePos(POS);
        player.gameMode.useItemOn(player, helper.getLevel(), upgrade, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        helper.assertBlockPresent(block(IC, "iron_chest"), POS);
        Container chest = (Container) helper.getBlockEntity(POS, BlockEntity.class);
        helper.assertTrue(chest.getContainerSize() == 54, "upgraded chest holds " + chest.getContainerSize() + " slots");
        helper.assertTrue(chest.getItem(3).is(Items.EMERALD) && chest.getItem(3).getCount() == 5, "upgrade lost the items: slot 3 holds " + chest.getItem(3));
        helper.succeed();
    }

    /** Right-clicking an iron chest opens its menu. */
    private static void chestsOpenAMenu(GameTestHelper helper) {
        if (skip(helper, IC)) return;
        helper.setBlock(POS, block(IC, "gold_chest"));
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel(); // has a connection, which opening a menu needs
        helper.useBlock(POS, player);
        helper.assertTrue(player.containerMenu != player.inventoryMenu, "no menu opened");
        helper.assertTrue(player.containerMenu.slots.size() == 81 + 36, "gold chest menu has " + player.containerMenu.slots.size() + " slots");
        helper.succeed();
    }
}
