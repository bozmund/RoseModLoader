package net.minecraft.world.level.storage.loot;

import net.minecraft.resources.Identifier;

/**
 * Era bridge (1.20.1): what {@code LootContext.getResolver()} returned, to look up other loot tables. 26.3 loot
 * tables live in the reloadable LOOT_TABLE registry (LootContextShim.getResolver).
 */
public interface LootDataResolver {
    /** The loot table with this id, or {@link LootTable#EMPTY}. */
    LootTable getLootTable(Identifier id);
}
