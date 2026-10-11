package rose.era.v1_20_1.mixin.client;

import java.util.List;
import java.util.Map;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.client.LegacyRecipeBooks;

/** After the client sorted its recipe book, mods' tabs get their recipes (see LegacyRecipeBooks). */
@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {
    @Shadow
    private Map<ExtendedRecipeBookCategory, List<RecipeCollection>> collectionsByTab;

    @Inject(method = "rebuildCollections", at = @At("TAIL"))
    private void rose$modTabs(CallbackInfo ci) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        collectionsByTab = Map.copyOf(LegacyRecipeBooks.withModTabs(collectionsByTab, connection.registryAccess()));
    }
}
