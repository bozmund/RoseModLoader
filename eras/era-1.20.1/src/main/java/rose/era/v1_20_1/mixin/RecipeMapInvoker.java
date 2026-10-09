package rose.era.v1_20_1.mixin;

import com.google.common.collect.Multimap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeMap.class)
public interface RecipeMapInvoker {
    @Invoker("<init>")
    static RecipeMap rose$create(Multimap<RecipeType<?>, RecipeHolder<?>> byType, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
        throw new AssertionError();
    }

    @Accessor("byKey")
    Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> rose$byKey();
}
