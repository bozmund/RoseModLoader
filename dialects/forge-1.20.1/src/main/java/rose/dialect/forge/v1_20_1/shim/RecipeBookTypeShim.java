package rose.dialect.forge.v1_20_1.shim;

import net.minecraft.world.inventory.RecipeBookType;
import rose.dialect.forge.v1_20_1.Unsupported;

/** Redirect targets for Forge's extensions of {@code RecipeBookType}. */
public final class RecipeBookTypeShim {
    /**
     * Forge 1.20.1 made {@code RecipeBookType} extensible ({@code create(name)} added an enum constant). 26.3's
     * enum is closed and its constants are sent over the network (recipe book settings), so a new constant would
     * desync clients. Custom books share the furnace book's open/filter state instead.
     */
    public static RecipeBookType create(String name) {
        Unsupported.feature("recipe-book-type:" + name, "custom recipe book type " + name + " shares the furnace book's settings");
        return RecipeBookType.FURNACE;
    }

    private RecipeBookTypeShim() {}
}
