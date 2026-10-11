package rose.era.v1_20_1.client;

import java.util.List;
import java.util.Map;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * 1.20.1 {@code RecipeBookCategories}: a client recipe book tab (an enum Forge let mods extend with {@code create}).
 * 26.3 has registry-backed RecipeBookCategory objects chosen by the server; mods' own tabs live on in Rose's legacy
 * recipe book (LegacyRecipeBooks), and vanilla's are kept for code that names them.
 */
public final class LegacyRecipeBookCategory {
    public static final LegacyRecipeBookCategory CRAFTING_SEARCH = vanilla("CRAFTING_SEARCH", new ItemStack(Items.COMPASS));
    public static final LegacyRecipeBookCategory CRAFTING_BUILDING_BLOCKS = vanilla("CRAFTING_BUILDING_BLOCKS", new ItemStack(Blocks.BRICKS));
    public static final LegacyRecipeBookCategory CRAFTING_REDSTONE = vanilla("CRAFTING_REDSTONE", new ItemStack(Items.REDSTONE));
    public static final LegacyRecipeBookCategory CRAFTING_EQUIPMENT = vanilla("CRAFTING_EQUIPMENT", new ItemStack(Items.IRON_AXE), new ItemStack(Items.GOLDEN_SWORD));
    public static final LegacyRecipeBookCategory CRAFTING_MISC = vanilla("CRAFTING_MISC", new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.APPLE));
    public static final LegacyRecipeBookCategory FURNACE_SEARCH = vanilla("FURNACE_SEARCH", new ItemStack(Items.COMPASS));
    public static final LegacyRecipeBookCategory FURNACE_FOOD = vanilla("FURNACE_FOOD", new ItemStack(Items.PORKCHOP));
    public static final LegacyRecipeBookCategory FURNACE_BLOCKS = vanilla("FURNACE_BLOCKS", new ItemStack(Blocks.STONE));
    public static final LegacyRecipeBookCategory FURNACE_MISC = vanilla("FURNACE_MISC", new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.EMERALD));
    public static final LegacyRecipeBookCategory BLAST_FURNACE_SEARCH = vanilla("BLAST_FURNACE_SEARCH", new ItemStack(Items.COMPASS));
    public static final LegacyRecipeBookCategory BLAST_FURNACE_BLOCKS = vanilla("BLAST_FURNACE_BLOCKS", new ItemStack(Blocks.REDSTONE_ORE));
    public static final LegacyRecipeBookCategory BLAST_FURNACE_MISC = vanilla("BLAST_FURNACE_MISC", new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.GOLDEN_LEGGINGS));
    public static final LegacyRecipeBookCategory SMOKER_SEARCH = vanilla("SMOKER_SEARCH", new ItemStack(Items.COMPASS));
    public static final LegacyRecipeBookCategory SMOKER_FOOD = vanilla("SMOKER_FOOD", new ItemStack(Items.PORKCHOP));
    public static final LegacyRecipeBookCategory STONECUTTER = vanilla("STONECUTTER", new ItemStack(Items.CHISELED_STONE_BRICKS));
    public static final LegacyRecipeBookCategory SMITHING = vanilla("SMITHING", new ItemStack(Items.NETHERITE_CHESTPLATE));
    public static final LegacyRecipeBookCategory CAMPFIRE = vanilla("CAMPFIRE", new ItemStack(Items.PORKCHOP));
    public static final LegacyRecipeBookCategory UNKNOWN = vanilla("UNKNOWN", new ItemStack(Items.BARRIER));

    public static final List<LegacyRecipeBookCategory> SMOKER_CATEGORIES = List.of(SMOKER_SEARCH, SMOKER_FOOD);
    public static final List<LegacyRecipeBookCategory> BLAST_FURNACE_CATEGORIES = List.of(BLAST_FURNACE_SEARCH, BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC);
    public static final List<LegacyRecipeBookCategory> FURNACE_CATEGORIES = List.of(FURNACE_SEARCH, FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC);
    public static final List<LegacyRecipeBookCategory> CRAFTING_CATEGORIES = List.of(CRAFTING_SEARCH, CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE);
    public static final Map<LegacyRecipeBookCategory, List<LegacyRecipeBookCategory>> AGGREGATE_CATEGORIES = Map.of(
            CRAFTING_SEARCH, List.of(CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE),
            FURNACE_SEARCH, List.of(FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC),
            BLAST_FURNACE_SEARCH, List.of(BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC),
            SMOKER_SEARCH, List.of(SMOKER_FOOD));

    private final String name;
    private final List<ItemStack> icons;

    private LegacyRecipeBookCategory(String name, List<ItemStack> icons) {
        this.name = name;
        this.icons = List.copyOf(icons);
    }

    private static LegacyRecipeBookCategory vanilla(String name, ItemStack... icons) {
        return new LegacyRecipeBookCategory(name, List.of(icons));
    }

    /** Forge's {@code RecipeBookCategories.create(name, icons...)}. */
    public static LegacyRecipeBookCategory create(String name, ItemStack... icons) {
        return new LegacyRecipeBookCategory(name, List.of(icons));
    }

    public static List<LegacyRecipeBookCategory> getCategories(RecipeBookType type) {
        return switch (type) {
            case CRAFTING -> CRAFTING_CATEGORIES;
            case FURNACE -> FURNACE_CATEGORIES;
            case BLAST_FURNACE -> BLAST_FURNACE_CATEGORIES;
            case SMOKER -> SMOKER_CATEGORIES;
        };
    }

    public List<ItemStack> getIconItems() {
        return icons;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
