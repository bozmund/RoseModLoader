package rose.era.v1_20_1.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

/**
 * Era bridge (1.20.1): what CustomRecipe looked like (an id and a book category given to the constructor).
 * Mod classes that extended CustomRecipe are re-parented here (rules/forge-1.20.1/superclasses.tsv).
 */
public abstract class LegacyCustomRecipe extends CustomRecipe {
    private final Identifier id;
    private final CraftingBookCategory category;

    public LegacyCustomRecipe(Identifier id, CraftingBookCategory category) {
        this.id = id;
        this.category = category;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }
}
