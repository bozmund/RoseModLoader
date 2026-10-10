package net.minecraft.client.renderer.item;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

/**
 * Era bridge (1.20.1): the client's item properties, registered by mods (vanilla's own became 26.x item model
 * properties). Item model definitions packfix made from old {@code overrides} read them through LegacyItemProperty.
 */
public final class ItemProperties {
    private static final Map<Identifier, ItemPropertyFunction> GENERIC = new HashMap<>();
    private static final Map<Item, Map<Identifier, ItemPropertyFunction>> PROPERTIES = new HashMap<>();

    public static ClampedItemPropertyFunction registerGeneric(Identifier name, ClampedItemPropertyFunction property) {
        GENERIC.put(name, property);
        return property;
    }

    public static void register(Item item, Identifier name, ClampedItemPropertyFunction property) {
        PROPERTIES.computeIfAbsent(item, k -> new HashMap<>()).put(name, property);
    }

    /** Forge 1.20.1's public overload, for any ItemPropertyFunction. */
    public static ItemPropertyFunction register(Item item, Identifier name, ItemPropertyFunction property) {
        PROPERTIES.computeIfAbsent(item, k -> new HashMap<>()).put(name, property);
        return property;
    }

    public static @Nullable ItemPropertyFunction getProperty(Item item, Identifier name) {
        Map<Identifier, ItemPropertyFunction> own = PROPERTIES.get(item);
        ItemPropertyFunction property = own != null ? own.get(name) : null;
        return property != null ? property : GENERIC.get(name);
    }

    private ItemProperties() {}
}
