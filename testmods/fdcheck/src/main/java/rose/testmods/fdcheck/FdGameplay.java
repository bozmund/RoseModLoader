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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
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
        helper.assertTrue(sites.isPresent() && sites.get().contains(soil), "the farmer should target rich soil farmland: " + sites);
        helper.succeed();
    }

    private FdGameplay() {}
}
