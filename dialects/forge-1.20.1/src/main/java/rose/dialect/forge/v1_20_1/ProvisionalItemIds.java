package rose.dialect.forge.v1_20_1;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import rose.dialect.forge.v1_20_1.mixin.DataComponentInitializersAccessor;
import rose.dialect.forge.v1_20_1.mixin.ItemAccessor;
import rose.dialect.forge.v1_20_1.mixin.ItemPropertiesAccessor;

/**
 * Items a mod constructs inside a {@code RegisterEvent} consumer before naming them
 * ({@code helper.register(name, new Item(...))}): 26.3 needs the id while the item is built, so it gets a provisional
 * one, and when the helper names it the id-derived parts (description id, item_name, item_model, component
 * initializer) are rebuilt for the real id.
 */
public final class ProvisionalItemIds {
    private static final AtomicInteger NEXT = new AtomicInteger();
    private static final Map<Item.Properties, ResourceKey<Item>> PROVISIONAL = new IdentityHashMap<>();
    private static final Map<Item, Item.Properties> BUILT = new IdentityHashMap<>();
    /** The component initializer each such item's constructor added (the last one, as the constructor returns). */
    private static final Map<Item, Object> INITIALIZERS = new IdentityHashMap<>();
    private static int open;

    /** While a RegisterEvent consumer runs for items, unnamed items get provisional ids. */
    public static void open() {
        open++;
    }

    public static void close() {
        open--;
        if (open == 0) {
            PROVISIONAL.clear();
            BUILT.clear();
            INITIALIZERS.clear();
        }
    }

    /** A provisional id for {@code properties}, or {@code null} outside a RegisterEvent consumer. */
    public static synchronized ResourceKey<Item> provisional(Item.Properties properties) {
        if (open == 0) return null;
        return PROVISIONAL.computeIfAbsent(properties, p ->
                ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("rose", "provisional/" + NEXT.incrementAndGet())));
    }

    public static synchronized void constructed(Item item, Item.Properties properties) {
        if (!PROVISIONAL.containsKey(properties)) return;
        BUILT.put(item, properties);
        var initializers = ((DataComponentInitializersAccessor) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).rose$initializers();
        INITIALIZERS.put(item, initializers.getLast());
    }

    /** {@code item} is being registered as {@code key}: if it was built with a provisional id, rebuild what depended on it. */
    public static synchronized void named(Object value, ResourceKey<?> key) {
        if (!(value instanceof Item item)) return;
        Item.Properties properties = BUILT.remove(item);
        if (properties == null) return;
        PROVISIONAL.remove(properties);
        @SuppressWarnings("unchecked")
        ResourceKey<Item> real = (ResourceKey<Item>) key;
        ItemPropertiesAccessor access = (ItemPropertiesAccessor) properties;
        access.rose$setId(real);
        String descriptionId = access.rose$effectiveDescriptionId();
        ((ItemAccessor) item).rose$setDescriptionId(descriptionId);
        var initializers = ((DataComponentInitializersAccessor) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).rose$initializers();
        Object stale = INITIALIZERS.remove(item);
        initializers.removeIf(entry -> entry == stale);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(real,
                access.rose$finalizeInitializer(Component.translatable(descriptionId), properties.effectiveModel()));
    }

    private ProvisionalItemIds() {}
}
