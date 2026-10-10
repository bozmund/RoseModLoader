package rose.testmods.fdcheck;

import static rose.testmods.fdcheck.FdCheck.POS;
import static rose.testmods.fdcheck.FdCheck.block;
import static rose.testmods.fdcheck.FdCheck.call;
import static rose.testmods.fdcheck.FdCheck.item;
import static rose.testmods.fdcheck.FdCheck.setSlot;
import static rose.testmods.fdcheck.FdCheck.skip;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import rose.api.gametest.RoseGameTests;

/**
 * FD gameplay on Rose: each test does one thing a player does with FD's blocks and items, so the 1.20.1 code behind
 * it runs (and must not crash). Behavior per FD 1.20.1's own code.
 */
final class FdGameplay {
    static void register() {
        test("cooking_pot_drops_with_its_meal", FdGameplay::cookingPotDropsWithItsMeal);
        test("lit_stove_burns_what_stands_on_it", FdGameplay::litStoveBurns);
        test("pie_slices_can_be_eaten", FdGameplay::pieSlicesCanBeEaten);
        test("safety_net_breaks_falls", FdGameplay::safetyNetBreaksFalls);
        test("tomatoes_are_picked_by_hand", FdGameplay::tomatoesArePickedByHand);
        test("cabinets_open", FdGameplay::cabinetsOpen);
        test("rotten_tomatoes_can_be_thrown", FdGameplay::rottenTomatoesCanBeThrown);
        test("rice_can_be_used", FdGameplay::riceCanBeUsed);
        test("bone_meal_on_fd_plants", FdGameplay::boneMealOnFdPlants);
        test("nourishment_stops_exhaustion", FdGameplay::nourishmentStopsExhaustion);
        test("knives_wear_when_hitting", FdGameplay::knivesWearWhenHitting);
        test("tatami_mat_breaks_whole", FdGameplay::tatamiMatBreaksWhole);
        test("baskets_pick_up_items", FdGameplay::basketsPickUpItems);
        test("skillet_cooks_on_a_stove", FdGameplay::skilletCooksOnAStove);
        test("cooking_pot_meals_can_be_taken", FdGameplay::cookingPotMealsCanBeTaken);
        test("skillet_cooks_held_food", FdGameplay::skilletCooksHeldFood);
        test("block_entities_keep_their_items", FdGameplay::blockEntitiesKeepTheirItems);
        test("pick_block_on_fd_blocks", FdGameplay::pickBlockOnFdBlocks);
        test("farmers_work_rich_soil_farmland", FdGameplay::farmersWorkRichSoilFarmland);
        test("skillet_is_a_weapon", FdGameplay::skilletIsAWeapon);
        test("drinks_are_drunk", FdGameplay::drinksAreDrunk);
        test("tools_repair_with_their_material", FdGameplay::toolsRepairWithTheirMaterial);
        test("farmers_buy_fd_crops", FdGameplay::farmersBuyFdCrops);
        test("plants_grow_on_rich_soil_farmland", FdGameplay::plantsGrowOnRichSoilFarmland);
        test("global_loot_modifiers_apply", FdGameplay::globalLootModifiersApply);
        test("trampled_rich_soil_stays_rich", FdGameplay::trampledRichSoilStaysRich);
        test("dispensers_cut_on_cutting_boards", FdGameplay::dispensersCutOnCuttingBoards);
        test("trees_keep_rich_soil", FdGameplay::treesKeepRichSoil);
    }

    private static void test(String name, Consumer<GameTestHelper> test) {
        RoseGameTests.register(Identifier.fromNamespaceAndPath("fdcheck", name), test);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState with(BlockState state, String property, String value) {
        for (Property<?> p : state.getProperties()) {
            if (p.getName().equals(property)) {
                return state.setValue((Property) p, (Comparable) p.getValue(value).orElseThrow());
            }
        }
        throw new IllegalArgumentException(state + " has no " + property);
    }

    private static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    /** Breaking a cooking pot that holds a meal drops the pot (FD's copy_meal loot function keeps the meal). */
    private static void cookingPotDropsWithItsMeal(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS, BlockEntity.class);
        setSlot(call(pot, "getInventory"), 6, new ItemStack(item("beef_stew"), 2));
        helper.getLevel().destroyBlock(helper.absolutePos(POS), true, player(helper));
        helper.succeedWhen(() -> helper.assertItemEntityPresent(item("cooking_pot"), POS, 2.0));
    }

    /** A lit stove hurts mobs standing on it (FD StoveBlock.stepOn). */
    private static void litStoveBurns(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockState stove = with(block("stove").defaultBlockState(), "lit", "true");
        helper.setBlock(POS, stove);
        Pig pig = helper.spawn(EntityTypes.PIG, POS.above());
        float before = pig.getHealth();
        stove.getBlock().stepOn(helper.getLevel(), helper.absolutePos(POS), stove, pig);
        helper.assertTrue(pig.getHealth() < before, "the stove should burn the pig: health " + pig.getHealth() + " of " + before);
        helper.succeed();
    }

    /** Right-clicking a pie with an appetite eats a slice (FD PieBlock). */
    private static void pieSlicesCanBeEaten(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("apple_pie"));
        Player player = player(helper);
        player.getFoodData().setFoodLevel(2);
        helper.useBlock(POS, player);
        helper.assertTrue(player.getFoodData().getFoodLevel() > 2, "eating a slice should feed: food " + player.getFoodData().getFoodLevel());
        helper.succeed();
    }

    /** Falling onto a safety net does no damage; onto a rice bale, little (FD SafetyNetBlock, RiceBaleBlock). */
    private static void safetyNetBreaksFalls(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("safety_net"));
        Pig pig = helper.spawn(EntityTypes.PIG, POS.above());
        float health = pig.getHealth();
        BlockState net = helper.getBlockState(POS);
        net.getBlock().fallOn(helper.getLevel(), net, helper.absolutePos(POS), pig, 20.0);
        helper.assertTrue(pig.getHealth() == health, "a safety net should break the fall: health " + pig.getHealth() + " of " + health);

        helper.setBlock(POS, block("rice_bale"));
        BlockState bale = helper.getBlockState(POS);
        bale.getBlock().fallOn(helper.getLevel(), bale, helper.absolutePos(POS), pig, 20.0);
        helper.assertTrue(pig.getHealth() > health - 19, "a rice bale should soften the fall: health " + pig.getHealth());
        helper.succeed();
    }

    /** Ripe tomatoes on a vine are picked with an empty hand, and the vine stays (FD TomatoBlock). */
    private static void tomatoesArePickedByHand(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS.below(), Blocks.FARMLAND);
        helper.setBlock(POS, block("tomatoes").defaultBlockState());
        BlockState vine = helper.getBlockState(POS);
        for (Property<?> p : vine.getProperties()) {
            if (p.getName().equals("age")) vine = with(vine, "age", String.valueOf(p.getPossibleValues().size() - 1));
        }
        helper.setBlock(POS, vine);
        helper.useBlock(POS, player(helper));
        helper.succeedWhen(() -> helper.assertItemEntityPresent(item("tomato"), POS, 3.0));
    }

    /** Using a cabinet opens its menu (FD CabinetBlock / CabinetBlockEntity). */
    private static void cabinetsOpen(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("oak_cabinet"));
        Player player = helper.makeMockServerPlayerInLevel(); // a mock Player can't open server menus
        helper.useBlock(POS, player);
        helper.assertTrue(player.containerMenu != player.inventoryMenu, "the cabinet's menu should be open");
        helper.succeed();
    }

    /** A rotten tomato can be thrown (FD RottenTomatoItem / RottenTomatoEntity). */
    private static void rottenTomatoesCanBeThrown(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = player(helper);
        var at = helper.absolutePos(POS.above());
        player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5); // in the test area, where entities can spawn
        ItemStack tomato = new ItemStack(item("rotten_tomato"));
        player.setItemInHand(InteractionHand.MAIN_HAND, tomato);
        tomato.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("farmersdelight", "rotten_tomato"));
        // Checked at once: the tomato splats and disappears as soon as it lands.
        helper.assertTrue(!helper.getLevel().getEntities(type, e -> true).isEmpty(), "no rotten tomato was thrown");
        helper.succeed();
    }

    /** Using rice (FD RiceItem, a 1.20.1 ItemNameBlockItem with its own use) doesn't fail. */
    private static void riceCanBeUsed(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = player(helper);
        ItemStack rice = new ItemStack(item("rice"));
        player.setItemInHand(InteractionHand.MAIN_HAND, rice);
        rice.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.succeed();
    }

    /** Bone meal works on FD's sandy shrub and rich soil (FD bonemeal code). */
    private static void boneMealOnFdPlants(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS.below(), Blocks.SAND);
        helper.setBlock(POS, block("sandy_shrub"));
        BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL, 8), helper.getLevel(), helper.absolutePos(POS));
        helper.setBlock(POS.below(), Blocks.AIR);
        helper.setBlock(POS, block("rich_soil"));
        BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL, 8), helper.getLevel(), helper.absolutePos(POS));
        helper.succeed();
    }

    /** Nourishment takes away food exhaustion (FD NourishmentEffect.applyEffectTick). */
    private static void nourishmentStopsExhaustion(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = player(helper);
        var nourishment = BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("farmersdelight", "nourishment")).orElseThrow();
        player.addEffect(new MobEffectInstance(nourishment, 600));
        player.getFoodData().addExhaustion(3.0F);
        nourishment.value().applyEffectTick(helper.getLevel(), player, 0);
        float exhaustion = exhaustion(player.getFoodData());
        helper.assertTrue(exhaustion < 3.0F, "Nourishment should take away exhaustion, left " + exhaustion);
        helper.succeed();
    }

    private static float exhaustion(FoodData food) {
        try {
            var field = FoodData.class.getDeclaredField("exhaustionLevel");
            field.setAccessible(true);
            return field.getFloat(food);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Hitting a mob with a knife wears the knife (FD KnifeItem.hurtEnemy). */
    private static void knivesWearWhenHitting(GameTestHelper helper) {
        if (skip(helper)) return;
        Player player = player(helper);
        ItemStack knife = new ItemStack(item("iron_knife"));
        player.setItemInHand(InteractionHand.MAIN_HAND, knife);
        Pig pig = helper.spawn(EntityTypes.PIG, POS.above());
        knife.hurtEnemy(pig, player);
        knife.postHurtEnemy(pig, player);
        helper.assertTrue(knife.getDamageValue() > 0, "the knife should wear when it hits, damage " + knife.getDamageValue());
        helper.succeed();
    }

    /** Breaking one half of a tatami mat removes the other (FD TatamiMatBlock.playerWillDestroy). */
    private static void tatamiMatBreaksWhole(GameTestHelper helper) {
        if (skip(helper)) return;
        BlockState mat = block("full_tatami_mat").defaultBlockState();
        BlockState head = with(with(mat, "facing", "east"), "part", "head");
        BlockState foot = with(with(mat, "facing", "east"), "part", "foot");
        helper.setBlock(POS, foot);
        helper.setBlock(POS.east(), head);
        Player player = player(helper);
        BlockPos footPos = helper.absolutePos(POS);
        foot.getBlock().playerWillDestroy(helper.getLevel(), footPos, foot, player);
        helper.getLevel().removeBlock(footPos, false);
        helper.succeedWhen(() -> helper.assertBlockNotPresent(block("full_tatami_mat"), POS.east()));
    }

    /** A basket facing up pulls in items dropped on it (FD BasketBlockEntity). */
    private static void basketsPickUpItems(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, with(block("bamboo_basket").defaultBlockState(), "facing", "up"));
        ItemEntity cabbage = helper.spawnItem(item("cabbage"), POS.above());
        helper.succeedWhen(() -> {
            Container basket = (Container) helper.getBlockEntity(POS, BlockEntity.class);
            helper.assertTrue(!basket.getItem(0).isEmpty() || cabbage.isRemoved() && !basket.isEmpty(), "the basket should hold the cabbage");
        });
    }

    /** A skillet on a lit stove cooks raw beef into steak (FD SkilletBlockEntity, campfire recipes). */
    private static void skilletCooksOnAStove(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, with(block("stove").defaultBlockState(), "lit", "true"));
        helper.setBlock(POS.above(), block("skillet"));
        BlockEntity skillet = helper.getBlockEntity(POS.above(), BlockEntity.class);
        try {
            skillet.getClass().getMethod("addItemToCook", ItemStack.class, Player.class).invoke(skillet, new ItemStack(Items.BEEF), player(helper));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        helper.succeedWhen(() -> {
            ItemStack stored = (ItemStack) call(skillet, "getStoredStack");
            boolean cooked = stored.is(Items.COOKED_BEEF);
            if (!cooked) {
                helper.assertItemEntityPresent(Items.COOKED_BEEF, POS.above(), 3.0);
            }
        });
    }

    /** Taking a finished meal out of the cooking pot's output slot (FD CookingPotResultSlot) puts it in the inventory. */
    private static void cookingPotMealsCanBeTaken(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS, BlockEntity.class);
        setSlot(call(pot, "getInventory"), 8, new ItemStack(item("beef_stew"), 2));
        Player player = helper.makeMockServerPlayerInLevel();
        helper.useBlock(POS, player);
        helper.assertTrue(player.containerMenu != player.inventoryMenu, "the cooking pot's menu should be open");
        player.containerMenu.quickMoveStack(player, 8);
        helper.assertTrue(player.getInventory().contains(new ItemStack(item("beef_stew"))), "the meal should be in the inventory");
        helper.succeed();
    }

    /** A skillet in hand cooks the food held in the other hand (FD SkilletItem) and hands back the cooked food. */
    private static void skilletCooksHeldFood(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, with(block("stove").defaultBlockState(), "lit", "true")); // the skillet needs heat nearby
        Player player = helper.makeMockServerPlayerInLevel();
        var at = helper.absolutePos(POS.above());
        player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        ItemStack skillet = new ItemStack(item("skillet"));
        player.setItemInHand(InteractionHand.MAIN_HAND, skillet);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BEEF));
        skillet.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        skillet.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.getInventory().contains(new ItemStack(Items.COOKED_BEEF)), "the skillet should hand back cooked beef");
        helper.succeed();
    }

    /** Cabinets, baskets and stoves keep their items when saved and loaded (FD's 1.20.1 load/saveAdditional). */
    private static void blockEntitiesKeepTheirItems(GameTestHelper helper) {
        if (skip(helper)) return;
        var registries = helper.getLevel().registryAccess();
        helper.setBlock(POS, block("oak_cabinet"));
        Container cabinet = (Container) helper.getBlockEntity(POS, BlockEntity.class);
        cabinet.setItem(3, new ItemStack(item("cabbage"), 5));
        BlockEntity savedCabinet = (BlockEntity) cabinet;
        Container cabinetAgain = (Container) BlockEntity.loadStatic(savedCabinet.getBlockPos(), savedCabinet.getBlockState(),
                savedCabinet.saveWithFullMetadata(registries), registries);
        helper.assertTrue(cabinetAgain != null && cabinetAgain.getItem(3).is(item("cabbage")) && cabinetAgain.getItem(3).getCount() == 5,
                "the cabinet lost its cabbages: " + (cabinetAgain == null ? null : cabinetAgain.getItem(3)));

        helper.setBlock(POS, with(block("stove").defaultBlockState(), "lit", "false"));
        BlockEntity stove = helper.getBlockEntity(POS, BlockEntity.class);
        setSlot(call(stove, "getItems"), 0, new ItemStack(Items.BEEF));
        BlockEntity stoveAgain = BlockEntity.loadStatic(stove.getBlockPos(), stove.getBlockState(), stove.saveWithFullMetadata(registries), registries);
        ItemStack onStove = stoveAgain == null ? ItemStack.EMPTY : stackIn(call(stoveAgain, "getItems"), 0);
        helper.assertTrue(onStove.is(Items.BEEF), "the stove lost its beef: " + onStove);
        helper.succeed();
    }

    private static ItemStack stackIn(Object handler, int slot) {
        try {
            return (ItemStack) handler.getClass().getMethod("getStackInSlot", int.class).invoke(handler, slot);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Pick-block on FD blocks that call 1.20.1 super.getCloneItemStack (cooking pot, skillet). */
    private static void pickBlockOnFdBlocks(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String name : java.util.List.of("cooking_pot", "skillet")) {
            helper.setBlock(POS, block(name));
            if (name.equals("skillet")) { // a placed skillet remembers its item; pick-block gives that item back
                BlockEntity skillet = helper.getBlockEntity(POS, BlockEntity.class);
                try {
                    skillet.getClass().getMethod("setSkilletItem", ItemStack.class).invoke(skillet, new ItemStack(item("skillet")));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
            ItemStack picked = helper.getBlockState(POS).getCloneItemStack(helper.getLevel(), helper.absolutePos(POS), true);
            helper.assertTrue(picked.is(item(name)), "pick-block on " + name + " gave " + picked);
        }
        helper.succeed();
    }

    /** Farmer villagers count rich soil farmland as work land (FD VillagersTargetRichSoilMixin on SecondaryPoiSensor). */
    private static void farmersWorkRichSoilFarmland(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("rich_soil_farmland"));
        Villager farmer = helper.spawn(EntityTypes.VILLAGER, POS.above());
        farmer.setVillagerData(farmer.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.FARMER));
        SecondaryPoiSensor sensor = new SecondaryPoiSensor();
        for (int i = 0; i <= 40; i++) sensor.tick(helper.getLevel(), farmer); // the sensor scans every 40 ticks
        GlobalPos soil = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(POS));
        var sites = farmer.getBrain().getMemory(MemoryModuleType.SECONDARY_JOB_SITE);
        farmer.discard(); // a farmer left behind wanders into other tests and picks up their items
        helper.assertTrue(sites.isPresent() && sites.get().contains(soil), "the farmer should target rich soil farmland: " + sites);
        helper.succeed();
    }

    /**
     * The skillet's 1.20.1 overrides become its components: getDefaultAttributeModifiers (7 attack damage in the main
     * hand), getEnchantmentValue (14).
     */
    private static void skilletIsAWeapon(GameTestHelper helper) {
        if (skip(helper)) return;
        ItemStack skillet = new ItemStack(item("skillet"));
        var modifiers = skillet.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double[] damage = {0};
        modifiers.forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) damage[0] += modifier.amount();
        });
        helper.assertTrue(damage[0] == 7.0, "the skillet should add 7 attack damage in the main hand: " + modifiers);
        Enchantable enchantable = skillet.get(DataComponents.ENCHANTABLE);
        helper.assertTrue(enchantable != null && enchantable.value() == 14, "the skillet should be enchantable (14): " + enchantable);
        helper.succeed();
    }

    /** FD's drinks (DrinkableItem: the DRINK use animation) are consumed as drinks, food or not, keeping food effects. */
    private static void drinksAreDrunk(GameTestHelper helper) {
        if (skip(helper)) return;
        for (String name : java.util.List.of("hot_cocoa", "milk_bottle", "apple_cider", "bone_broth")) {
            Consumable consumable = new ItemStack(item(name)).get(DataComponents.CONSUMABLE);
            helper.assertTrue(consumable != null && consumable.animation() == ItemUseAnimation.DRINK
                    && consumable.sound().is(SoundEvents.GENERIC_DRINK.key()) && !consumable.hasConsumeParticles(), name + " should be a drink: " + consumable);
        }
        Consumable cider = new ItemStack(item("apple_cider")).get(DataComponents.CONSUMABLE);
        helper.assertTrue(!cider.onConsumeEffects().isEmpty(), "apple cider keeps its food effect: " + cider);
        helper.succeed();
    }

    /** Knives repair with their tier's 26.3 material tag; the skillet with iron (its 1.20.1 isValidRepairItem). */
    private static void toolsRepairWithTheirMaterial(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.assertTrue(new ItemStack(item("iron_knife")).isValidRepairItem(new ItemStack(Items.IRON_INGOT)), "iron knives repair with iron");
        helper.assertTrue(new ItemStack(item("golden_knife")).isValidRepairItem(new ItemStack(Items.GOLD_INGOT)), "golden knives repair with gold");
        helper.assertTrue(new ItemStack(item("skillet")).isValidRepairItem(new ItemStack(Items.IRON_INGOT)), "skillets repair with iron");
        helper.assertTrue(!new ItemStack(item("skillet")).isValidRepairItem(new ItemStack(Items.DIAMOND)), "skillets don't repair with diamonds");
        helper.succeed();
    }

    /**
     * Novice farmers can buy onions and tomatoes (FD VillagerEvents.onVillagerTrades adds them to level 1 through
     * Forge's VillagerTradesEvent). Each farmer gets 2 of the level's 7 trades, so some of 20 farmers must have one.
     */
    private static void farmersBuyFdCrops(GameTestHelper helper) {
        if (skip(helper)) return;
        boolean found = false;
        for (int i = 0; i < 20 && !found; i++) {
            Villager farmer = helper.spawn(EntityTypes.VILLAGER, POS.above());
            farmer.setVillagerData(farmer.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.FARMER));
            for (var offer : farmer.getOffers()) {
                ItemStack cost = offer.getBaseCostA();
                if (cost.is(item("onion")) || cost.is(item("tomato"))) found = true;
            }
            farmer.discard();
        }
        helper.assertTrue(found, "no farmer offered to buy onions or tomatoes");
        helper.succeed();
    }

    /** Vanilla crops and flowers can stand on rich soil farmland (FD RichSoilFarmlandBlock.canSustainPlant: CROP, PLAINS). */
    private static void plantsGrowOnRichSoilFarmland(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("rich_soil_farmland"));
        for (Block plant : java.util.List.of(Blocks.WHEAT, Blocks.POPPY)) {
            boolean survives = plant.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(POS.above()));
            helper.assertTrue(survives, plant + " should survive on rich soil farmland");
        }
        helper.assertTrue(!Blocks.CACTUS.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(POS.above())),
                "cactus shouldn't survive on rich soil farmland");
        helper.succeed();
    }

    /**
     * FD's Forge global loot modifiers change drops: mature rice cut with a knife also drops straw
     * (straw_from_mature_rice), and a pie broken with a knife drops its slices (slicing_apple_pie).
     */
    private static void globalLootModifiersApply(GameTestHelper helper) {
        if (skip(helper)) return;
        ItemStack knife = new ItemStack(item("iron_knife"));
        BlockState rice = with(block("rice_panicles").defaultBlockState(), "age", "3");
        var riceDrops = Block.getDrops(rice, helper.getLevel(), helper.absolutePos(POS), null, null, knife);
        helper.assertTrue(riceDrops.stream().anyMatch(s -> s.is(item("straw"))), "mature rice cut with a knife should drop straw: " + riceDrops);
        var pieDrops = Block.getDrops(block("apple_pie").defaultBlockState(), helper.getLevel(), helper.absolutePos(POS), null, null, knife);
        helper.assertTrue(pieDrops.stream().anyMatch(s -> s.is(item("apple_pie_slice"))), "a pie broken with a knife should drop slices: " + pieDrops);
        // add_loot_simple_dungeon: dungeon chests also roll FD's chests/fd_simple_dungeon (forge:loot_table_id)
        var dungeon = helper.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        var chestLoot = dungeon.getRandomItems(new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(net.minecraft.world.phys.Vec3.atCenterOf(POS))).create(LootContextParamSets.CHEST));
        helper.assertTrue(chestLoot.stream().anyMatch(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals("farmersdelight")),
                "a dungeon chest should hold FD loot: " + chestLoot);
        helper.succeed();
    }

    /** Rich soil farmland can't be trampled: FD's fallOn only hurts the falling entity (RichSoilFarmlandBlock.fallOn). */
    private static void trampledRichSoilStaysRich(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("rich_soil_farmland"));
        Pig pig = helper.spawn(EntityTypes.PIG, POS.above());
        float health = pig.getHealth();
        BlockState soil = helper.getBlockState(POS);
        soil.getBlock().fallOn(helper.getLevel(), soil, helper.absolutePos(POS), pig, 6.0);
        boolean hurt = pig.getHealth() < health;
        pig.discard();
        helper.assertTrue(helper.getBlockState(POS).is(block("rich_soil_farmland")), "rich soil farmland was trampled into " + helper.getBlockState(POS));
        helper.assertTrue(hurt, "the fall should hurt the pig");
        helper.succeed();
    }

    /** A dispenser facing a cutting board uses its tool on the board's item (FD CuttingBoardDispenserMixin, rebased). */
    private static void dispensersCutOnCuttingBoards(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, with(Blocks.DISPENSER.defaultBlockState(), "facing", "east"));
        helper.setBlock(POS.east(), block("cutting_board"));
        BlockEntity board = helper.getBlockEntity(POS.east(), BlockEntity.class);
        try {
            board.getClass().getMethod("addItem", ItemStack.class).invoke(board, new ItemStack(item("cabbage")));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        Container dispenser = (Container) helper.getBlockEntity(POS, BlockEntity.class);
        dispenser.setItem(0, new ItemStack(item("iron_knife")));
        helper.getBlockState(POS).tick(helper.getLevel(), helper.absolutePos(POS), helper.getLevel().getRandom());
        ItemStack stored = (ItemStack) call(board, "getStoredItem");
        helper.assertTrue(stored.isEmpty(), "the dispenser's knife should have cut the cabbage, the board still holds " + stored);
        helper.assertTrue(dispenser.getItem(0).is(item("iron_knife")), "the knife should stay in the dispenser: " + dispenser.getItem(0));
        helper.succeed();
    }

    /**
     * A tree grown on rich soil leaves the rich soil under its trunk: FD's KeepRichSoilTreeMixin (rebased onto
     * placeBelowTrunkBlock) and 26.3 itself (#cannot_replace_below_tree_trunk holds #dirt, which FD puts rich soil in).
     */
    private static void treesKeepRichSoil(GameTestHelper helper) {
        if (skip(helper)) return;
        helper.setBlock(POS, block("rich_soil"));
        helper.setBlock(POS.above(), Blocks.OAK_SAPLING);
        var pos = helper.absolutePos(POS.above());
        for (int i = 0; i < 2; i++) {
            BlockState sapling = helper.getLevel().getBlockState(pos);
            if (sapling.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock grower) {
                grower.advanceTree(helper.getLevel(), pos, sapling, helper.getLevel().getRandom());
            }
        }
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.OAK_LOG), "the oak should have grown: " + helper.getLevel().getBlockState(pos));
        helper.assertTrue(helper.getBlockState(POS).is(block("rich_soil")), "the trunk turned the rich soil into " + helper.getBlockState(POS));
        helper.succeed();
    }

    private FdGameplay() {}
}
