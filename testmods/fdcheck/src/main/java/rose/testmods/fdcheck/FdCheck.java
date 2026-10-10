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
    static final BlockPos POS = new BlockPos(1, 1, 1);

    @Override
    public void onInitialize() {
        FdGameplay.register();
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
        RoseGameTests.register(id("ingredient_lists_keep_all_alternatives"), FdCheck::ingredientListsKeepAllAlternatives);
        RoseGameTests.register(id("foods_keep_their_own_effects"), FdCheck::foodsKeepTheirOwnEffects);
        RoseGameTests.register(id("drinks_can_be_drunk"), FdCheck::drinksCanBeDrunk);
        RoseGameTests.register(id("crops_and_soil_random_tick"), FdCheck::cropsAndSoilRandomTick);
        RoseGameTests.register(id("meal_tooltips_build"), FdCheck::mealTooltipsBuild);
        RoseGameTests.register(id("knife_slices_cake"), FdCheck::knifeSlicesCake);
        RoseGameTests.register(id("rabbit_stew_gives_jump_boost"), FdCheck::rabbitStewGivesJumpBoost);
        RoseGameTests.register(id("dog_food_heals_tamed_wolves"), FdCheck::dogFoodHealsTamedWolves);
        RoseGameTests.register(id("villages_include_compost_piles"), FdCheck::villagesIncludeCompostPiles);
        RoseGameTests.register(id("backstabbing_enchants_knives"), FdCheck::backstabbingEnchantsKnives);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("fdcheck", path);
    }

    static boolean skip(GameTestHelper helper) {
        if (RoseLoader.get().mod("farmersdelight").isPresent()) return false;
        helper.succeed(); // Farmer's Delight not installed
        return true;
    }

    static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("farmersdelight", path));
    }

    static Item item(String path) {
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

    /**
     * A 1.20.1 ingredient list ("any of these tags or items") keeps all its alternatives: in a vanilla crafting recipe
     * (barbecue stick: any cooked meat or fish, upgraded by packfix) and in FD's own cooking recipe (cabbage rolls:
     * raw meat, fish, vegetables or mushrooms).
     */
    private static void ingredientListsKeepAllAlternatives(GameTestHelper helper) {
        if (skip(helper)) return;
        assertAccepts(helper, "barbecue_stick", net.minecraft.world.item.Items.COOKED_PORKCHOP, net.minecraft.world.item.Items.COOKED_RABBIT);
        assertAccepts(helper, "cooking/cabbage_rolls", net.minecraft.world.item.Items.COD, net.minecraft.world.item.Items.CARROT);
        helper.succeed();
    }

    private static void assertAccepts(GameTestHelper helper, String recipePath, Item... items) {
        var key = net.minecraft.resources.ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("farmersdelight", recipePath));
        var recipe = helper.getLevel().getServer().getRecipeManager().byKey(key);
        helper.assertTrue(recipe.isPresent(), "recipe missing: " + key);
        var ingredients = recipe.get().value().placementInfo().ingredients();
        for (Item item : items) {
            helper.assertTrue(ingredients.stream().anyMatch(i -> i.test(new ItemStack(item))),
                    recipePath + " should accept " + item + "; ingredients: " + ingredients);
        }
    }

    /**
     * Each food keeps its own 1.20.1 extras (FD FoodValues): wheat dough, raw pasta and chicken cuts have a 30% chance
     * of Hunger; wheat dough isn't fast to eat, chicken cuts are. Wheat dough and raw pasta have the same nutrition
     * and saturation, which once made their extras collide.
     */
    private static void foodsKeepTheirOwnEffects(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String food : List.of("wheat_dough", "raw_pasta", "chicken_cuts")) {
            var consumable = new ItemStack(item(food)).get(DataComponents.CONSUMABLE);
            helper.assertTrue(consumable != null && consumable.onConsumeEffects().stream().anyMatch(e ->
                    e instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect apply && apply.probability() == 0.3F
                            && apply.effects().stream().anyMatch(i -> i.getEffect().is(net.minecraft.world.effect.MobEffects.HUNGER))),
                    food + " should have a 30% chance of Hunger, has " + consumable);
        }
        float doughSeconds = new ItemStack(item("wheat_dough")).get(DataComponents.CONSUMABLE).consumeSeconds();
        float cutsSeconds = new ItemStack(item("chicken_cuts")).get(DataComponents.CONSUMABLE).consumeSeconds();
        helper.assertTrue(doughSeconds == 1.6F && cutsSeconds == 0.8F, "eating time: wheat dough " + doughSeconds + " (1.6), chicken cuts " + cutsSeconds + " (0.8)");
        helper.succeed();
    }

    /**
     * FD's drinks (DrinkableItem) can be drunk: use starts drinking with the drink animation, and finishing
     * hot cocoa takes away a harmful effect (FD HotCocoaItem) and leaves a bottle.
     */
    private static void drinksCanBeDrunk(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack cocoa = new ItemStack(item("hot_cocoa"));
        player.setItemInHand(InteractionHand.MAIN_HAND, cocoa);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 600));
        cocoa.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.isUsingItem(), "using hot cocoa didn't start drinking");
        helper.assertTrue(cocoa.getUseAnimation() == net.minecraft.world.item.ItemUseAnimation.DRINK, "hot cocoa animation: " + cocoa.getUseAnimation());
        ItemStack left = cocoa.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(!player.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "hot cocoa should take away Slowness");
        helper.assertTrue(left.is(net.minecraft.world.item.Items.GLASS_BOTTLE) || player.getInventory().contains(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE)),
                "no bottle back, left " + left);

        // Apple cider is a drink and a food: drinking it goes through the food checks (Forge getFoodProperties).
        Player thirsty = helper.makeMockPlayer(GameType.SURVIVAL);
        thirsty.getFoodData().setFoodLevel(2);
        ItemStack cider = new ItemStack(item("apple_cider"));
        thirsty.setItemInHand(InteractionHand.MAIN_HAND, cider);
        cider.use(helper.getLevel(), thirsty, InteractionHand.MAIN_HAND);
        helper.assertTrue(thirsty.isUsingItem(), "using apple cider didn't start drinking");
        helper.succeed();
    }

    /**
     * FD's crops and rich soil survive random ticks (growth, hydration): their 1.20.1 code calls Forge's level and
     * block state extensions (isAreaLoaded, canSustainPlant, isFertile, canBeHydrated).
     */
    private static void cropsAndSoilRandomTick(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockPos soil = POS;
        BlockPos crop = POS.above();
        helper.setBlock(soil, block("rich_soil_farmland"));
        helper.setBlock(crop, block("budding_tomatoes"));
        var level = helper.getLevel();
        for (int i = 0; i < 20; i++) {
            level.getBlockState(helper.absolutePos(soil)).randomTick(level, helper.absolutePos(soil), level.getRandom());
            level.getBlockState(helper.absolutePos(crop)).randomTick(level, helper.absolutePos(crop), level.getRandom());
        }
        BlockState tomatoes = level.getBlockState(helper.absolutePos(crop));
        helper.assertTrue(tomatoes.canSurvive(level, helper.absolutePos(crop)), "budding tomatoes should stay on rich soil farmland");
        helper.setBlock(soil, net.minecraft.world.level.block.Blocks.STONE);
        helper.assertTrue(!block("budding_tomatoes").defaultBlockState().canSurvive(level, helper.absolutePos(crop)),
                "budding tomatoes shouldn't stay on stone");

        helper.setBlock(soil, net.minecraft.world.level.block.Blocks.FARMLAND);
        helper.setBlock(crop, block("rice"));
        for (int i = 0; i < 20; i++) {
            level.getBlockState(helper.absolutePos(crop)).randomTick(level, helper.absolutePos(crop), level.getRandom());
        }
        helper.succeed();
    }

    /** FD meal tooltips build (ConsumableItem lists the meal's effects through FD TextUtils and Forge getFoodProperties). */
    private static void mealTooltipsBuild(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var lines = new ItemStack(item("beef_stew")).getTooltipLines(Item.TooltipContext.of(helper.getLevel()), player,
                net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        helper.assertTrue(lines.size() > 1, "beef stew tooltip should list Nourishment, has " + lines);
        helper.succeed();
    }

    /** Forge RightClickBlock: FD's KnifeEvents.onCakeInteraction slices a cake with a knife, dropping a cake slice. */
    private static void knifeSlicesCake(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, net.minecraft.world.level.block.Blocks.CAKE);
        var player = (net.minecraft.server.level.ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        ItemStack knife = new ItemStack(item("iron_knife"));
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        BlockPos pos = helper.absolutePos(POS);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        player.gameMode.useItemOn(player, helper.getLevel(), knife, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(POS, net.minecraft.world.level.block.CakeBlock.BITES, 1);
        helper.assertItemEntityPresent(item("cake_slice"), POS, 2.0);
        helper.succeed();
    }

    /** Forge LivingEntityUseItemEvent.Finish: FD's CommonEvents.handleVanillaSoupEffects gives Jump Boost for rabbit stew. */
    private static void rabbitStewGivesJumpBoost(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.RABBIT_STEW));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        try {
            Method complete = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("completeUsingItem");
            complete.setAccessible(true);
            complete.invoke(player);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP_BOOST),
                "rabbit stew should give Jump Boost, effects: " + player.getActiveEffects());
        helper.succeed();
    }

    /** Forge EntityInteract: FD's DogFoodEvent heals a tamed wolf fed dog food. */
    private static void dogFoodHealsTamedWolves(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var wolf = helper.spawn(net.minecraft.world.entity.EntityTypes.WOLF, POS);
        wolf.tame(player);
        wolf.setHealth(1.0F);
        ItemStack food = new ItemStack(item("dog_food"));
        player.setItemInHand(InteractionHand.MAIN_HAND, food);
        player.interactOn(wolf, InteractionHand.MAIN_HAND, wolf.position());
        helper.assertTrue(wolf.getHealth() == wolf.getMaxHealth(), "dog food should heal the wolf, health " + wolf.getHealth());
        helper.succeed();
    }

    /**
     * FD's VillageStructures (ServerAboutToStartEvent) adds its compost piles to the village house pools, through
     * fields its Forge access transformer opened up; the piece's structure file must load too.
     */
    private static void villagesIncludeCompostPiles(GameTestHelper helper) {
        if (skip(helper)) return;
        var server = helper.getLevel().getServer();
        var pool = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
                .getValue(Identifier.withDefaultNamespace("village/plains/houses"));
        helper.assertTrue(pool != null, "no plains houses pool");
        String piece = "farmersdelight:village/houses/plains_compost_pile";
        helper.assertTrue(pool.getTemplates().stream().anyMatch(p -> p.getFirst().toString().contains(piece)),
                "compost pile not in the pool's templates");
        helper.assertTrue(pool.getShuffledTemplates(helper.getLevel().getRandom()).stream().anyMatch(e -> e.toString().contains(piece)),
                "compost pile not among the pieces villages generate from");
        helper.assertTrue(server.getStructureTemplateManager().get(Identifier.parse(piece)).isPresent(), "structure " + piece + " doesn't load");
        helper.succeed();
    }

    /**
     * FD's Backstabbing (a 1.20.1 Enchantment subclass) is in the server's data-driven enchantment registry, offered by
     * the enchanting table for knives only, and FD's LivingHurtEvent listener multiplies a knife hit from behind by
     * 1.2 + 0.2 * level (BackstabbingEnchantment.getBackstabbingDamagePerLevel).
     */
    private static void backstabbingEnchantsKnives(GameTestHelper helper) {
        if (skip(helper)) return;
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var key = net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("farmersdelight", "backstabbing"));
        var backstabbing = registry.get(key).orElse(null);
        helper.assertTrue(backstabbing != null, "farmersdelight:backstabbing isn't in the enchantment registry");
        helper.assertTrue(backstabbing.is(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE), "not offered by the enchanting table");
        helper.assertTrue(backstabbing.value().canEnchant(new ItemStack(item("iron_knife"))), "can't enchant a knife; supported: " + backstabbing.value().definition().supportedItems());
        helper.assertFalse(backstabbing.value().canEnchant(new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)), "enchants a sword");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack knife = new ItemStack(item("iron_knife"));
        knife.enchant(backstabbing, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        float front = damageFrom(helper, player, 2.0);
        float back = damageFrom(helper, player, -2.0);
        helper.assertTrue(Math.abs(front - 2.0F) < 0.01F, "a hit from the front should deal 2, dealt " + front);
        helper.assertTrue(Math.abs(back - 2.8F) < 0.01F, "a backstab should deal 2 * 1.4, dealt " + back);
        helper.succeed();
    }

    /** Damage a fresh pig facing south (+z) takes from a 2-point hit by {@code player} standing {@code dz} away. */
    private static float damageFrom(GameTestHelper helper, Player player, double dz) {
        var pig = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, POS);
        pig.setYRot(0.0F);
        pig.setYHeadRot(0.0F);
        pig.setXRot(0.0F);
        player.setPos(pig.getX(), pig.getY(), pig.getZ() + dz);
        float before = pig.getHealth();
        pig.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 2.0F);
        return before - pig.getHealth();
    }

    private static boolean hasEffect(Player player, String path) {
        var effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("farmersdelight", path));
        return effect.isPresent() && player.hasEffect(effect.get());
    }

    static void setSlot(Object handler, int slot, ItemStack stack) {
        try {
            Method set = handler.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            set.invoke(handler, slot, stack);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Object call(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            return m.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't call " + method + " on " + target, e);
        }
    }
}
