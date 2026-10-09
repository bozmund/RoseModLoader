package net.minecraftforge.event.village;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraftforge.eventbus.api.Event;

/** Lets mods add trades per villager level (1-5). Trades are Forge ItemListings (e.g. BasicItemListing). */
public class VillagerTradesEvent extends Event {
    protected Int2ObjectMap<List<Object>> trades;
    protected VillagerProfession type;

    public VillagerTradesEvent(Int2ObjectMap<List<Object>> trades, VillagerProfession type) {
        this.trades = trades;
        this.type = type;
    }

    public Int2ObjectMap<List<Object>> getTrades() {
        return trades;
    }

    public VillagerProfession getType() {
        return type;
    }
}
