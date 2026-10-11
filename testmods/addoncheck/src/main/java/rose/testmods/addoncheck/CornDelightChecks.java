package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.POS;
import static rose.testmods.addoncheck.AddonCheck.block;
import static rose.testmods.addoncheck.AddonCheck.call;
import static rose.testmods.addoncheck.AddonCheck.item;
import static rose.testmods.addoncheck.AddonCheck.setSlot;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.slot;
import static rose.testmods.addoncheck.AddonCheck.test;
import static rose.testmods.addoncheck.AddonCheck.with;

import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Corn Delight 1.2.11 (a Farmer's Delight add-on, with mmlib): its content, crop, and FD machines using its recipes. */
final class CornDelightChecks {
    private static final String CD = "corn_delight";
    private static final String FD = "farmersdelight";

    private CornDelightChecks() {}

    static void register() {
        test("cd_content_registered", CornDelightChecks::contentRegistered);
        test("cd_cutting_board_cuts_corn", CornDelightChecks::cuttingBoardCutsCorn);
        test("cd_cooking_pot_boils_corn", CornDelightChecks::cookingPotBoilsCorn);
        test("cd_corn_seeds_smelt_into_popcorn", CornDelightChecks::seedsSmeltIntoPopcorn);
        test("cd_mmlib_villagers_share_corn", CornDelightChecks::villagersShareCorn);
        test("cd_recipes_are_in_the_recipe_registry", CornDelightChecks::recipesAreInTheRegistry);
    }

    private static Item cd(String path) {
        return item(CD, path);
    }

    private static void contentRegistered(GameTestHelper helper) {
        if (skip(helper, CD, FD)) return;
        for (String i : List.of("corn", "corn_seeds", "corncob", "boiled_corn", "grilled_corn", "cornbread", "popcorn",
                "caramel_popcorn", "tortilla", "taco", "corn_dog", "corn_soup", "nachos_bowl", "cob_pipe")) {
            cd(i);
        }
        for (String b : List.of("corn_crate", "corn_kernel_bag", "popcorn_box", "wild_corn", "nachos_block")) {
            helper.assertTrue(block(CD, b).asItem() == cd(b), b + " isn't linked to its item");
        }
        block(CD, "corn_crop");
        helper.assertTrue(cd("cornbread").components().has(net.minecraft.core.component.DataComponents.FOOD), "cornbread isn't food");
        helper.succeed();
    }

    /** farmersdelight:cutting with a forge:crops/corn ingredient: corn cut with a knife gives two seeds and a cob. */
    private static void cuttingBoardCutsCorn(GameTestHelper helper) {
        if (skip(helper, CD, FD)) return;
        helper.setBlock(POS, block(FD, "cutting_board"));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(cd("corn")));
        helper.useBlock(POS, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(FD, "iron_knife")));
        helper.useBlock(POS, player);
        helper.succeedWhen(() -> {
            helper.assertItemEntityCountIs(cd("corn_seeds"), POS, 2.0, 2);
            helper.assertItemEntityCountIs(cd("corncob"), POS, 2.0, 1);
        });
    }

    private static void cookingPotBoilsCorn(GameTestHelper helper) {
        if (skip(helper, CD, FD)) return;
        var recipe = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(CD, "cooking/boiled_corn"));
        helper.assertTrue(helper.getLevel().recipeAccess().byKey(recipe).isPresent(), "recipe corn_delight:cooking/boiled_corn isn't loaded");
        helper.setBlock(POS, with(block(FD, "stove").defaultBlockState(), "lit", "true"));
        helper.setBlock(POS.above(), block(FD, "cooking_pot"));
        BlockEntity pot = helper.getBlockEntity(POS.above(), BlockEntity.class);
        Object inventory = call(pot, "getInventory");
        setSlot(inventory, 0, new ItemStack(cd("corn")));
        // boiled corn needs no container, so it goes straight to the output slot (8)
        helper.succeedWhen(() -> helper.assertTrue(slot(inventory, 8).is(cd("boiled_corn")),
                "no boiled corn yet; the pot holds " + pot.saveWithoutMetadata(helper.getLevel().registryAccess())));
    }

    private static void seedsSmeltIntoPopcorn(GameTestHelper helper) {
        if (skip(helper, CD, FD)) return;
        var input = new SingleRecipeInput(new ItemStack(cd("corn_seeds")));
        ItemStack result = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, input, helper.getLevel())
                .map(r -> r.value().assemble(input)).orElse(null);
        helper.assertTrue(result != null && result.is(cd("popcorn")), "corn seeds smelt into " + result);
        helper.succeed();
    }

    /**
     * mmlib's TradeWithVillager mixin makes villagers throw half of the food it registered (Corn Delight's corn) to
     * farmers, through a static @Invoker of 1.20.1 throwHalfStack(Villager, Set, LivingEntity).
     */
    private static void villagersShareCorn(GameTestHelper helper) {
        if (skip(helper, CD, "mysterious_mountain_lib")) return;
        var owner = helper.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER, POS.above());
        var farmer = helper.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER, POS.above().east(3));
        owner.setNoAi(true);
        farmer.setNoAi(true);
        owner.getInventory().addItem(new ItemStack(cd("corn"), 40));
        Class<?> invoker = AddonCheck.modClass("cn.mcmod_mmf.mmlib.mixin.TradeWithVillagerInvoker");
        AddonCheck.callStatic(invoker, "throwHalfStackInvoker", new Class<?>[] {
                net.minecraft.world.entity.npc.villager.Villager.class, java.util.Set.class, net.minecraft.world.entity.LivingEntity.class},
                owner, java.util.Set.of(cd("corn")), farmer);
        helper.assertTrue(owner.getInventory().countItem(cd("corn")) == 20, "the villager kept " + owner.getInventory().countItem(cd("corn")) + " corn");
        helper.succeedWhen(() -> helper.assertItemEntityPresent(cd("corn"), POS.above(), 6.0));
    }

    /**
     * Recipes are registry elements in 26.3; advancements (recipe rewards, recipe_unlocked) and /recipe find them by
     * key in the registry, not through the recipe manager.
     */
    private static void recipesAreInTheRegistry(GameTestHelper helper) {
        if (skip(helper, CD)) return;
        var recipes = helper.getLevel().getServer().reloadableRegistries().lookup().lookupOrThrow(net.minecraft.core.registries.Registries.RECIPE);
        for (String id : List.of("minecraft:bread", "corn_delight:cooking/boiled_corn", "corn_delight:popcorn")) {
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, net.minecraft.resources.Identifier.parse(id));
            helper.assertTrue(recipes.get(key).isPresent(), "the recipe registry has no " + id);
        }
        helper.succeed();
    }
}
