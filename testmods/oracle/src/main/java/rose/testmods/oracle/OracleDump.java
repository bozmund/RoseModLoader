package rose.testmods.oracle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Writes what a running server knows about some mods' content as JSON: registry entries, block properties, item
 * components, recipes, loot tables and tags. It uses only vanilla 26.3 classes, so the same code runs inside Rose and
 * inside a Fabric 26.3 reference server; {@code OracleDiff} compares the two dumps.
 */
public final class OracleDump {
    /** The files a dump consists of, without {@code .json}. */
    public static final List<String> FILES = List.of("registries", "blocks", "items", "recipes", "loot_tables", "tags");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final MinecraftServer server;
    private final Set<String> namespaces;

    public OracleDump(MinecraftServer server, Set<String> namespaces) {
        this.server = server;
        this.namespaces = namespaces;
    }

    /** Dumps when {@code -Drose.oracle.out=<dir>} is set; {@code -Drose.oracle.namespaces=a,b} picks the mods. */
    public static boolean requested() {
        return System.getProperty("rose.oracle.out") != null;
    }

    public static void runFromProperties(MinecraftServer server) {
        Path out = Path.of(System.getProperty("rose.oracle.out"));
        Set<String> namespaces = Set.of(System.getProperty("rose.oracle.namespaces", "farmersdelight").split(","));
        try {
            new OracleDump(server, namespaces).writeTo(out);
            System.out.println("[oracle] dump written to " + out.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("oracle dump failed", e);
        }
    }

    public void writeTo(Path dir) throws IOException {
        Files.createDirectories(dir);
        write(dir.resolve("registries.json"), registries());
        write(dir.resolve("blocks.json"), blocks());
        write(dir.resolve("items.json"), items());
        write(dir.resolve("recipes.json"), recipes());
        write(dir.resolve("loot_tables.json"), lootTables());
        write(dir.resolve("tags.json"), tags());
    }

    private static void write(Path file, JsonObject json) throws IOException {
        Files.writeString(file, GSON.toJson(json) + "\n");
    }

    private boolean ours(Identifier id) {
        return id != null && namespaces.contains(id.getNamespace());
    }

    /** Every static and dynamic registry (the server's registry access holds both): the ids the chosen mods added. */
    private JsonObject registries() {
        TreeMap<String, JsonArray> out = new TreeMap<>();
        server.registryAccess().registries().forEach(entry -> addIds(out, entry.value()));
        return toObject(out);
    }

    private void addIds(TreeMap<String, JsonArray> out, Registry<?> registry) {
        List<String> ids = registry.keySet().stream().filter(this::ours).map(Identifier::toString).sorted().toList();
        if (ids.isEmpty()) return;
        JsonArray array = new JsonArray();
        ids.forEach(array::add);
        out.put(registry.key().identifier().toString(), array);
    }

    private JsonObject blocks() {
        TreeMap<String, JsonObject> out = new TreeMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (!ours(id)) continue;
            BlockState state = block.defaultBlockState();
            JsonObject json = new JsonObject();
            json.addProperty("destroy_time", block.defaultDestroyTime());
            json.addProperty("explosion_resistance", block.getExplosionResistance());
            json.addProperty("friction", block.getFriction());
            json.addProperty("speed_factor", block.getSpeedFactor());
            json.addProperty("jump_factor", block.getJumpFactor());
            json.addProperty("map_color", block.defaultMapColor().id);
            json.addProperty("item", String.valueOf(BuiltInRegistries.ITEM.getKey(block.asItem())));
            SoundType sound = state.getSoundType();
            json.addProperty("sound", sound.getBreakSound().location() + " / " + sound.getPlaceSound().location());
            json.addProperty("light_emission", state.getLightEmission());
            json.addProperty("requires_correct_tool", state.requiresCorrectToolForDrops());
            json.addProperty("ignited_by_lava", state.ignitedByLava());
            json.addProperty("can_occlude", state.canOcclude());
            json.addProperty("randomly_ticking", state.isRandomlyTicking());
            json.addProperty("piston_push_reaction", state.getPistonPushReaction().name());
            json.add("properties", properties(block));
            json.add("default_state", defaultState(state));
            out.put(id.toString(), json);
        }
        return toObject(out);
    }

    private static JsonObject properties(Block block) {
        TreeMap<String, JsonArray> out = new TreeMap<>();
        for (Property<?> property : block.getStateDefinition().getProperties()) {
            out.put(property.getName(), possibleValues(property));
        }
        return toObject(out);
    }

    private static <T extends Comparable<T>> JsonArray possibleValues(Property<T> property) {
        JsonArray values = new JsonArray();
        property.getPossibleValues().forEach(v -> values.add(property.getName(v)));
        return values;
    }

    private static JsonObject defaultState(BlockState state) {
        JsonObject json = new JsonObject();
        state.getValues()
                .sorted(Comparator.comparing(v -> v.property().getName()))
                .forEach(v -> json.addProperty(v.property().getName(), v.valueName()));
        return json;
    }

    /** Each item's default components, the 26.3 home of stack size, durability, food, rarity and so on. */
    private JsonObject items() {
        RegistryOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        TreeMap<String, JsonObject> out = new TreeMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!ours(id)) continue;
            TreeMap<String, JsonElement> components = new TreeMap<>();
            for (TypedDataComponent<?> component : item.components()) {
                if (component.type().isTransient()) continue;
                components.put(String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type())), orError(component.encodeValue(ops)));
            }
            JsonObject json = new JsonObject();
            json.add("components", toObject(components));
            out.put(id.toString(), json);
        }
        return toObject(out);
    }

    private JsonObject recipes() {
        RegistryOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        TreeMap<String, JsonElement> out = new TreeMap<>();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            Identifier id = holder.id().identifier();
            if (!ours(id)) continue;
            out.put(id.toString(), resolveItemTags(encode(holder.value(), ops)));
        }
        return toObject(out);
    }

    /**
     * Replaces each item tag reference ({@code "#c:crops/wheat"}) with the items in it, so recipes compare by what
     * they accept, not by tag names: Forge 1.20.1's {@code forge:} tags became {@code c:} tags. A tag with no items
     * stays a name, marked {@code (empty)}; a tag with one item compares like that item (as in {@link #itemsOf}).
     */
    private JsonElement resolveItemTags(JsonElement json) {
        if (json instanceof JsonObject object) {
            JsonObject out = new JsonObject();
            object.entrySet().forEach(e -> out.add(e.getKey(), resolveItemTags(e.getValue())));
            return out;
        }
        if (json instanceof JsonArray array) {
            JsonArray out = new JsonArray();
            array.forEach(e -> out.add(resolveItemTags(e)));
            return out;
        }
        if (json instanceof JsonPrimitive p && p.isString() && p.getAsString().startsWith("#")) {
            Identifier tag = Identifier.tryParse(p.getAsString().substring(1));
            if (tag == null) return json;
            TreeSet<String> items = new TreeSet<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, tag)).forEach(h ->
                    h.unwrapKey().ifPresent(k -> items.add(k.identifier().toString())));
            if (items.isEmpty()) return new JsonPrimitive(p.getAsString() + " (empty)");
            if (items.size() == 1) return new JsonPrimitive(items.first());
            JsonArray out = new JsonArray();
            items.forEach(out::add);
            return out;
        }
        return json;
    }

    /**
     * What the game sees of a recipe (type, serializer, recipe book info, ingredients, displays), plus its full JSON
     * under {@code codec}. The summary still compares when the full JSON can't be produced: Rose can't encode recipes
     * from 1.20.1 serializers.
     */
    private static JsonObject encode(Recipe<?> recipe, RegistryOps<JsonElement> ops) {
        JsonObject json = new JsonObject();
        json.add("type", guard(() -> new JsonPrimitive(String.valueOf(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType())))));
        json.add("serializer", guard(() -> new JsonPrimitive(String.valueOf(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer())))));
        json.add("group", guard(() -> new JsonPrimitive(recipe.group())));
        json.add("book_category", guard(() -> new JsonPrimitive(String.valueOf(BuiltInRegistries.RECIPE_BOOK_CATEGORY.getKey(recipe.recipeBookCategory())))));
        json.add("ingredients", guard(() -> {
            JsonArray ingredients = new JsonArray();
            recipe.placementInfo().ingredients().forEach(i -> ingredients.add(itemsOf(i)));
            return ingredients;
        }));
        json.add("display", guard(() -> {
            JsonArray displays = new JsonArray();
            recipe.display().forEach(d -> displays.add(orError(RecipeDisplay.CODEC.encodeStart(ops, d))));
            return displays;
        }));
        json.add("codec", guard(() -> orError(Recipe.DIRECT_CODEC.encodeStart(ops, recipe))));
        return json;
    }

    /** The items an ingredient accepts, sorted: an item and a tag holding only that item compare equal. */
    private static JsonArray itemsOf(Ingredient ingredient) {
        TreeSet<String> items = new TreeSet<>();
        ingredient.items().forEach(h -> h.unwrapKey().ifPresent(k -> items.add(k.identifier().toString())));
        JsonArray out = new JsonArray();
        items.forEach(out::add);
        return out;
    }

    private static JsonElement guard(java.util.function.Supplier<JsonElement> value) {
        try {
            return value.get();
        } catch (RuntimeException e) {
            return error(e.toString());
        }
    }

    private JsonObject lootTables() {
        HolderLookup.Provider lookup = server.reloadableRegistries().lookup();
        RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
        TreeMap<String, JsonElement> out = new TreeMap<>();
        lookup.lookupOrThrow(Registries.LOOT_TABLE).listElements().forEach(holder -> {
            Identifier id = holder.key().identifier();
            if (!ours(id)) return;
            out.put(id.toString(), guard(() -> orError(LootTable.DIRECT_CODEC.encodeStart(ops, holder.value()))));
        });
        return toObject(out);
    }

    /**
     * The chosen mods' own tags with all their members, plus other mods' and vanilla tags with only the members the
     * chosen mods added (FD putting its knives in {@code c:tools/knife}, for example). Keys: {@code registry #tag}.
     */
    private JsonObject tags() {
        TreeMap<String, JsonArray> out = new TreeMap<>();
        server.registryAccess().registries().forEach(entry -> addTags(out, entry));
        return toObject(out);
    }

    private <T> void addTags(TreeMap<String, JsonArray> out, RegistryAccess.RegistryEntry<T> entry) {
        Registry<T> registry = entry.value();
        registry.getTags().forEach(tag -> {
            boolean ownTag = ours(tag.key().location());
            List<String> members = members(tag, ownTag);
            if (members.isEmpty() && !ownTag) return;
            JsonArray array = new JsonArray();
            members.forEach(array::add);
            out.put(entry.key().identifier() + " #" + tag.key().location(), array);
        });
    }

    private <T> List<String> members(HolderSet.Named<T> tag, boolean all) {
        return tag.stream()
                .map(Holder::unwrapKey)
                .flatMap(java.util.Optional::stream)
                .map(ResourceKey::identifier)
                .filter(id -> all || ours(id))
                .map(Identifier::toString)
                .sorted()
                .toList();
    }

    private static JsonElement orError(DataResult<JsonElement> result) {
        return result.mapOrElse(Function.identity(), e -> error(e.message()));
    }

    private static JsonElement error(String message) {
        JsonObject json = new JsonObject();
        json.add("$error", new JsonPrimitive(message));
        return json;
    }

    private static <V extends JsonElement> JsonObject toObject(TreeMap<String, V> map) {
        JsonObject json = new JsonObject();
        map.forEach(json::add);
        return json;
    }
}
