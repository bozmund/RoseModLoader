package rose.era.v1_20_1.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Recipe book display {@code rose:legacy} of a 1.20.1 recipe of a mod recipe type. 1.20.1 sent clients whole recipes
 * and their recipe books sorted them into tabs on the client (Forge's category finders); 26.3 sends displays. This one
 * shows the inputs in order and the result, and carries the recipe's source (serializer, id, JSON) so the client can
 * rebuild the recipe for the mod's finder.
 */
public record LegacyRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation,
                                  Identifier serializer, Identifier recipe, String json) implements RecipeDisplay {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("rose", "legacy");
    public static final MapCodec<LegacyRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(LegacyRecipeDisplay::ingredients),
            SlotDisplay.CODEC.fieldOf("result").forGetter(LegacyRecipeDisplay::result),
            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(LegacyRecipeDisplay::craftingStation),
            Identifier.CODEC.fieldOf("serializer").forGetter(LegacyRecipeDisplay::serializer),
            Identifier.CODEC.fieldOf("recipe").forGetter(LegacyRecipeDisplay::recipe),
            Codec.STRING.fieldOf("json").forGetter(LegacyRecipeDisplay::json)).apply(i, LegacyRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LegacyRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), LegacyRecipeDisplay::ingredients,
            SlotDisplay.STREAM_CODEC, LegacyRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC, LegacyRecipeDisplay::craftingStation,
            Identifier.STREAM_CODEC, LegacyRecipeDisplay::serializer,
            Identifier.STREAM_CODEC, LegacyRecipeDisplay::recipe,
            ByteBufCodecs.STRING_UTF8, LegacyRecipeDisplay::json,
            LegacyRecipeDisplay::new);
    public static final RecipeDisplay.Type<LegacyRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);
    /** The book category of these recipes; the client sorts them into the mods' own tabs. */
    public static final RecipeBookCategory CATEGORY = new RecipeBookCategory();

    /** Registers the display type and the category (both sides, before registries freeze). */
    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_DISPLAY, ID, TYPE);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, ID, CATEGORY);
    }

    @Override
    public Type<LegacyRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return ingredients.stream().allMatch(d -> d.isEnabled(enabledFeatures)) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}
