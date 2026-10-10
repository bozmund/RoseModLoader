package rose.testmods.fdcheck;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import rose.api.ModInitializer;
import rose.api.gametest.RoseGameTests;
import rose.loader.RoseLoader;

/**
 * Behavior checks for Farmer's Delight 1.20.1 running through Rose's Forge dialect and era bridge. Each test
 * passes as skipped when FD isn't installed.
 */
public final class FdCheck implements ModInitializer {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @Override
    public void onInitialize() {
        RoseGameTests.register(id("content_registered"), FdCheck::contentRegistered);
        RoseGameTests.register(id("cutting_board_cuts_cabbage"), FdCheck::cuttingBoardCutsCabbage);
        RoseGameTests.register(id("cooking_pot_cooks_beef_stew"), FdCheck::cookingPotCooksBeefStew);
        RoseGameTests.register(id("crops_are_compostable"), FdCheck::cropsAreCompostable);
        RoseGameTests.register(id("animals_eat_fd_food"), FdCheck::animalsEatFdFood);
        RoseGameTests.register(id("crops_drop_by_loot_table"), FdCheck::cropsDropByLootTable);
        RoseGameTests.register(id("wild_cabbages_generate_on_beaches"), FdCheck::wildCabbagesGenerateOnBeaches);
        RoseGameTests.register(id("cooking_pot_item_keeps_its_meal"), FdCheck::cookingPotItemKeepsItsMeal);
        RoseGameTests.register(id("meals_feed_and_give_effects"), FdCheck::mealsFeedAndGiveEffects);
        RoseGameTests.register(id("block_items_named_after_blocks"), FdCheck::blockItemsNamedAfterBlocks);
        RoseGameTests.register(id("cabinets_burn_in_furnaces"), FdCheck::cabinetsBurnInFurnaces);
        RoseGameTests.register(id("cooking_recipes_have_ingredients"), FdCheck::cookingRecipesHaveIngredients);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("fdcheck", path);
    }

    private static boolean skip(GameTestHelper helper) {
        if (RoseLoader.get().mod("farmersdelight").isPresent()) return false;
        helper.succeed(); // Farmer's Delight not installed
        return true;
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farmersdelight", path));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("farmersdelight", path));
    }

    /** FD's main blocks and items exist (each was created by FD's own registration code). */
    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String b : List.of("stove", "cooking_pot", "cutting_board", "skillet", "bamboo_basket", "cabbages", "rice", "budding_tomatoes",
                "tomatoes", "rope", "rich_soil", "rich_soil_farmland", "organic_compost", "wild_cabbages", "rope_fence_gate")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("farmersdelight", b)), "block missing: " + b);
        }
        for (String i : List.of("cabbage", "tomato", "onion", "rice", "iron_knife", "flint_knife", "beef_stew", "cabbage_leaf",
                "tomato_seeds", "skillet", "cooking_pot", "hamburger")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("farmersdelight", i)), "item missing: " + i);
        }
        for (String b : List.of("cooking_pot", "stove", "cutting_board", "rich_soil")) {
            helper.assertTrue(block(b).asItem() == item(b), "block " + b + " isn't linked to its item (Item.BY_BLOCK)");
        }
        helper.assertTrue(BuiltInRegistries.TRIGGER_TYPES.containsKey(Identifier.fromNamespaceAndPath("farmersdelight", "use_cutting_board")),
                "criterion trigger missing: use_cutting_board");
        helper.succeed();
    }

    /** Place a cabbage on a cutting board (right-click), then cut it with a knife: two cabbage leaves drop. */
    private static void cuttingBoardCutsCabbage(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("cutting_board"));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("cabbage")));
        helper.useBlock(POS, player);
        BlockEntity board = helper.getBlockEntity(POS, BlockEntity.class);
        ItemStack stored = (ItemStack) call(board, "getStoredItem");
        helper.assertTrue(stored.is(item("cabbage")), "board should hold the cabbage, holds " + stored);
        // Saved and loaded again (world reload, client sync): the data survives.
        var registries = helper.getLevel().registryAccess();
        BlockEntity reloaded = BlockEntity.loadStatic(board.getBlockPos(), board.getBlockState(), board.saveWithFullMetadata(registries), registries);
        helper.assertTrue(reloaded != null && ((ItemStack) call(reloaded, "getStoredItem")).is(item("cabbage")), "board lost its cabbage when reloaded");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("iron_knife")));
        helper.useBlock(POS, player);
        helper.succeedWhen(() -> helper.assertItemEntityCountIs(item("cabbage_leaf"), POS, 2.0, 2));
    }

    /** A cooking pot on a lit stove turns beef, a carrot and a potato into beef stew. */
    private static void cookingPotCooksBeefStew(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockState stove = block("stove").defaultBlockState();
        for (Property<?> p : stove.getProperties()) {
            if (p.getName().equals("lit") && p instanceof BooleanProperty lit) stove = stove.setValue(lit, true);
        }
        helper.setBlock(POS, stove);
        helper.setBlock(POS.above(), block("cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS.above(), BlockEntity.class);
        Object inventory = call(pot, "getInventory");
        setSlot(inventory, 0, new ItemStack(net.minecraft.world.item.Items.BEEF));
        setSlot(inventory, 1, new ItemStack(net.minecraft.world.item.Items.CARROT));
        setSlot(inventory, 2, new ItemStack(net.minecraft.world.item.Items.POTATO));
        helper.succeedWhen(() -> {
            ItemStack meal = (ItemStack) call(pot, "getMeal");
            helper.assertTrue(meal.is(item("beef_stew")), "no beef stew yet, meal slot has " + meal);
        });
    }

    /** FD's common setup registers its crops with the composter (1.20.1 ComposterBlock.COMPOSTABLES). */
    private static void cropsAreCompostable(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String i : List.of("cabbage", "tomato", "onion", "rice", "cabbage_leaf", "tree_bark")) {
            helper.assertTrue(new ItemStack(item(i)).has(DataComponents.COMPOSTABLE), "not compostable: " + i);
        }
        helper.succeed();
    }

    /** FD's common setup adds its crops to animal and villager food (26.3: item tags and villager_food). */
    private static void animalsEatFdFood(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.assertTrue(new ItemStack(item("cabbage_seeds")).is(ItemTags.CHICKEN_FOOD), "chickens should eat cabbage seeds");
        helper.assertTrue(new ItemStack(item("cabbage")).is(ItemTags.PIG_FOOD), "pigs should eat cabbage");
        helper.assertTrue(new ItemStack(item("cod_slice")).is(ItemTags.CAT_FOOD), "cats should eat cod slices");
        helper.assertTrue(new ItemStack(item("rice")).is(ItemTags.PARROT_FOOD), "parrots should eat rice");
        helper.assertTrue(new ItemStack(item("onion")).is(ItemTags.VILLAGER_PICKS_UP), "villagers should pick up onions");
        helper.assertTrue(new ItemStack(item("tomato")).has(DataComponents.VILLAGER_FOOD), "tomatoes should be villager food");
        helper.assertTrue(new ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS).is(ItemTags.CHICKEN_FOOD), "vanilla chicken food kept");
        helper.succeed();
    }

    /** FD's 1.20.1 loot tables, upgraded by packfix: age and tool conditions decide the drops. */
    private static void cropsDropByLootTable(GameTestHelper helper) {
        if (skip(helper)) return;
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(POS);
        BlockState young = block("cabbages").defaultBlockState();
        BlockState mature = young;
        for (Property<?> p : young.getProperties()) {
            if (p.getName().equals("age") && p instanceof IntegerProperty age) mature = young.setValue(age, 7);
        }
        helper.assertFalse(dropped(young, level, pos, ItemStack.EMPTY).contains(item("cabbage")), "young cabbages must not drop a cabbage");
        helper.assertTrue(dropped(young, level, pos, ItemStack.EMPTY).contains(item("cabbage_seeds")), "young cabbages drop seeds");
        helper.assertTrue(dropped(mature, level, pos, ItemStack.EMPTY).contains(item("cabbage")), "mature cabbages drop a cabbage");

        BlockState wild = block("wild_cabbages").defaultBlockState();
        ItemStack shears = new ItemStack(net.minecraft.world.item.Items.SHEARS);
        helper.assertTrue(dropped(wild, level, pos, shears).contains(item("wild_cabbages")), "shears harvest wild cabbages (forge:can_tool_perform_action)");
        helper.assertFalse(dropped(wild, level, pos, ItemStack.EMPTY).contains(item("wild_cabbages")), "bare hands don't");
        helper.succeed();
    }

    private static Set<Item> dropped(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool) {
        Set<Item> items = new java.util.HashSet<>();
        for (int i = 0; i < 20; i++) { // drops are random; 20 rolls cover the guaranteed ones
            for (ItemStack s : Block.getDrops(state, level, pos, null, null, tool)) items.add(s.getItem());
        }
        return items;
    }

    /**
     * FD's worldgen: its biome modifier adds patch_wild_cabbages to beaches, and its wild_crop feature (a 1.20.1
     * Feature subclass with inline placed features) grows wild cabbages on sand.
     */
    private static void wildCabbagesGenerateOnBeaches(GameTestHelper helper) {
        if (skip(helper)) return;
        ServerLevel level = helper.getLevel();
        var access = level.registryAccess();
        Identifier patch = Identifier.fromNamespaceAndPath("farmersdelight", "patch_wild_cabbages");
        var placed = access.lookupOrThrow(Registries.PLACED_FEATURE).getValue(patch);
        helper.assertTrue(placed != null, "placed feature missing: " + patch);
        Biome beach = access.lookupOrThrow(Registries.BIOME).getValue(Biomes.BEACH);
        helper.assertTrue(beach.getGenerationSettings().hasFeature(placed), "beaches should have " + patch + " (biome modifier)");

        var feature = access.lookupOrThrow(Registries.FEATURE).getValue(patch);
        helper.assertTrue(feature != null, "configured feature missing: " + patch);
        BlockPos center = helper.absolutePos(POS).above(2);
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                for (int y = -3; y <= 3; y++) level.setBlock(center.offset(x, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(center.offset(x, -1, z), net.minecraft.world.level.block.Blocks.SAND.defaultBlockState(), 2);
            }
        }
        feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), center);
        int grown = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, 0, -7), center.offset(7, 0, 7))) {
            if (level.getBlockState(p).is(block("wild_cabbages"))) grown++;
        }
        helper.assertTrue(grown > 0, "no wild cabbages grew on the sand");
        helper.succeed();
    }

    /** A cooking pot picked up as an item keeps its meal (1.20.1 BlockEntityTag), and placing it restores it. */
    private static void cookingPotItemKeepsItsMeal(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockState potState = block("cooking_pot").defaultBlockState();
        helper.setBlock(POS, potState);
        BlockEntity pot = helper.getBlockEntity(POS, BlockEntity.class);
        setSlot(call(pot, "getInventory"), 6, new ItemStack(item("beef_stew"), 2)); // the meal slot
        ItemStack potItem = potState.getCloneItemStack(helper.getLevel(), helper.absolutePos(POS), true);
        helper.assertTrue(potItem.is(item("cooking_pot")), "pick-block gives a cooking pot, gave " + potItem);

        BlockPos other = POS.east(2);
        helper.setBlock(other, potState);
        boolean applied = net.minecraft.world.item.BlockItem.updateCustomBlockEntityTag(helper.getLevel(), null, helper.absolutePos(other), potItem);
        ItemStack meal = (ItemStack) call(helper.getBlockEntity(other, BlockEntity.class), "getMeal");
        helper.assertTrue(applied && meal.is(item("beef_stew")) && meal.getCount() == 2, "placed pot should hold 2 beef stew, holds " + meal);
        helper.succeed();
    }

    /** Eating an FD meal: beef stew feeds, gives a bowl back and Nourishment (FD's FoodValues.BEEF_STEW). */
    private static void mealsFeedAndGiveEffects(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(2);
        ItemStack stew = new ItemStack(item("beef_stew"));
        ItemStack left = stew.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.getFoodData().getFoodLevel() > 2, "stew didn't feed: food " + player.getFoodData().getFoodLevel());
        helper.assertTrue(left.is(net.minecraft.world.item.Items.BOWL) || player.getInventory().contains(new ItemStack(net.minecraft.world.item.Items.BOWL)),
                "no bowl back, left " + left);
        helper.assertTrue(hasEffect(player, "nourishment"), "beef stew should give Nourishment, effects: " + player.getActiveEffects());
        helper.succeed();
    }

    /**
     * Block items are named after their block, as in 1.20.1 (FD's lang file has only block.farmersdelight.* for them):
     * plain BlockItem (rich soil), BlockItem subclasses (cooking pot; rope extends FD's FuelBlockItem), double-high
     * (wild rice), sign and hanging sign. Rice is an ItemNameBlockItem and keeps its item name.
     */
    private static void blockItemsNamedAfterBlocks(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String name : List.of("rich_soil", "cooking_pot", "rope", "wild_rice", "canvas_sign", "hanging_canvas_sign")) {
            Item item = item(name);
            helper.assertTrue(item instanceof net.minecraft.world.item.BlockItem, name + " isn't a block item");
            String expected = ((net.minecraft.world.item.BlockItem) item).getBlock().getDescriptionId();
            helper.assertTrue(item.getDescriptionId().equals(expected), name + " is named " + item.getDescriptionId() + ", expected " + expected);
        }
        helper.assertTrue(item("rice").getDescriptionId().equals("item.farmersdelight.rice"), "rice is named " + item("rice").getDescriptionId());
        helper.succeed();
    }

    /** FD's wooden furniture burns in a furnace: FD sets its burn time the Forge way (getBurnTime, 300 ticks). */
    private static void cabinetsBurnInFurnaces(GameTestHelper helper) {
        if (skip(helper)) return;
        ItemStack cabinet = new ItemStack(item("oak_cabinet"));
        var fuel = cabinet.get(DataComponents.COOKING_FUEL);
        helper.assertTrue(fuel != null && fuel.burnTime() instanceof net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant c
                && c.value() == 300, "oak cabinet should burn 300 ticks, fuel: " + fuel);
        helper.setBlock(POS, net.minecraft.world.level.block.Blocks.FURNACE);
        var furnace = helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.class);
        furnace.setItem(0, new ItemStack(net.minecraft.world.item.Items.BEEF));
        furnace.setItem(1, cabinet);
        helper.succeedWhen(() -> helper.assertBlockProperty(POS, net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true));
    }

    /**
     * FD's cooking pot recipes expose their inputs (1.20.1 getIngredients) as 26.3 placement info; without them 26.3
     * ignores the recipe in its recipe sets ("can't be placed due to empty ingredients").
     */
    private static void cookingRecipesHaveIngredients(GameTestHelper helper) {
        if (skip(helper)) return;
        var key = net.minecraft.resources.ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("farmersdelight", "cooking/beef_stew"));
        var recipe = helper.getLevel().getServer().getRecipeManager().byKey(key);
        helper.assertTrue(recipe.isPresent(), "recipe missing: " + key);
        var placement = recipe.get().value().placementInfo();
        helper.assertTrue(!placement.isImpossibleToPlace() && placement.ingredients().size() == 3,
                "beef stew should have 3 ingredients (beef, carrot, potato), has " + placement.ingredients());
        helper.succeed();
    }

    private static boolean hasEffect(Player player, String path) {
        var effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("farmersdelight", path));
        return effect.isPresent() && player.hasEffect(effect.get());
    }

    private static void setSlot(Object handler, int slot, ItemStack stack) {
        try {
            Method set = handler.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            set.invoke(handler, slot, stack);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object call(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            return m.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + method + " on " + target, e);
        }
    }
}
