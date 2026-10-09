package net.minecraft.world.level.storage.loot.functions;

import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Era bridge (1.20.1): a loot function type wrapped its JSON serializer. 26.3 registers a MapCodec instead;
 * RegistryAdapters turns registered types into one (see LegacyLoot).
 */
public class LootItemFunctionType {
    private final Serializer<? extends LootItemFunction> serializer;

    public LootItemFunctionType(Serializer<? extends LootItemFunction> serializer) {
        this.serializer = serializer;
    }

    public Serializer<? extends LootItemFunction> getSerializer() {
        return serializer;
    }
}
