package rose.dialect.forge.v1_20_1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Forge 1.20.1 global loot modifiers (Forge's LootModifierManager and ForgeHooks.modifyLoot). The data pack lists
 * them in {@code forge:loot_modifiers/global_loot_modifiers.json} (every pack's list in order, {@code replace}
 * starting over); each is {@code <ns>:loot_modifiers/<path>.json}, decoded by the codec its {@code type} names in
 * Forge's global loot modifier serializer registry. After a loot table rolls its items (not for tables rolled inside
 * another), every modifier changes them in turn (LootTableMixin), knowing which table it is (forge:loot_table_id).
 */
public final class ForgeLootModifiers {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Identifier LIST = Identifier.fromNamespaceAndPath("forge", "loot_modifiers/global_loot_modifiers.json");
    private static final ThreadLocal<Identifier> QUERIED = new ThreadLocal<>();
    /** The modifiers' JSON from the last data reload, in order; decoded on first use (codecs need the registries). */
    private static volatile Map<Identifier, JsonElement> sources = Map.of();
    private static volatile List<IGlobalLootModifier> modifiers;
    private static final Map<LootTable, Identifier> TABLE_IDS = new com.google.common.collect.MapMaker().weakKeys().makeMap();

    /** The loot table whose items the modifiers are changing, or {@code null}. */
    public static Identifier queriedLootTable() {
        return QUERIED.get();
    }

    /** Forge's {@code ForgeHooks.modifyLoot}: the loot after every global loot modifier changed it. */
    public static ObjectArrayList<ItemStack> modify(LootTable table, ObjectArrayList<ItemStack> loot, LootContext context) {
        List<IGlobalLootModifier> all = modifiers(context);
        if (all.isEmpty()) return loot;
        Identifier previous = QUERIED.get();
        QUERIED.set(idOf(table, context));
        try {
            for (IGlobalLootModifier modifier : all) loot = modifier.apply(loot, context);
            return loot;
        } finally {
            QUERIED.set(previous);
        }
    }

    /** Whether any modifiers are loaded (so loot tables can skip the extra work). */
    public static boolean active() {
        return !sources.isEmpty();
    }

    private static List<IGlobalLootModifier> modifiers(LootContext context) {
        List<IGlobalLootModifier> decoded = modifiers;
        if (decoded != null) return decoded;
        synchronized (ForgeLootModifiers.class) {
            if (modifiers != null) return modifiers;
            RegistryOps<JsonElement> ops = context.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
            List<IGlobalLootModifier> out = new ArrayList<>();
            sources.forEach((id, json) -> {
                try {
                    out.add(decode(json, ops));
                } catch (RuntimeException | LinkageError e) {
                    LOG.warn("[rose/forge] skipped global loot modifier {}: {}", id, e.toString());
                }
            });
            LOG.info("[rose/forge] {} global loot modifier(s)", out.size());
            modifiers = List.copyOf(out);
            return modifiers;
        }
    }

    @SuppressWarnings("unchecked")
    private static IGlobalLootModifier decode(JsonElement json, RegistryOps<JsonElement> ops) {
        Identifier type = Identifier.parse(json.getAsJsonObject().get("type").getAsString());
        Codec<? extends IGlobalLootModifier> codec = (Codec<? extends IGlobalLootModifier>) ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().getValue(type);
        if (codec == null) throw new IllegalArgumentException("unknown global loot modifier type " + type);
        return codec.parse(ops, json).getOrThrow();
    }

    private static Identifier idOf(LootTable table, LootContext context) {
        Identifier id = TABLE_IDS.get(table);
        if (id != null) return id;
        context.getLevel().getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElements()
                .forEach(holder -> TABLE_IDS.put(holder.value(), holder.key().identifier()));
        return TABLE_IDS.get(table);
    }

    /** Reads the modifier list and files on each data reload (ReloadableServerResourcesMixin adds it). */
    public static final class Listener extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
        @Override
        protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
            List<Identifier> ids = new ArrayList<>();
            for (Resource list : manager.getResourceStack(LIST)) {
                try (Reader reader = list.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("replace") && json.get("replace").getAsBoolean()) ids.clear();
                    json.getAsJsonArray("entries").forEach(e -> {
                        Identifier id = Identifier.parse(e.getAsString());
                        if (!ids.contains(id)) ids.add(id);
                    });
                } catch (Exception e) {
                    LOG.warn("[rose/forge] couldn't read {} from {}: {}", LIST, list.sourcePackId(), e.toString());
                }
            }
            Map<Identifier, JsonElement> out = new LinkedHashMap<>();
            for (Identifier id : ids) {
                Identifier file = id.withPath(path -> "loot_modifiers/" + path + ".json");
                manager.getResource(file).ifPresentOrElse(resource -> {
                    try (Reader reader = resource.openAsReader()) {
                        out.put(id, JsonParser.parseReader(reader));
                    } catch (Exception e) {
                        LOG.warn("[rose/forge] couldn't read global loot modifier {}: {}", id, e.toString());
                    }
                }, () -> LOG.warn("[rose/forge] global loot modifier {} is listed but has no {}", id, file));
            }
            return out;
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
            sources = Map.copyOf(prepared);
            modifiers = null;
            TABLE_IDS.clear();
        }
    }

    private ForgeLootModifiers() {}
}
