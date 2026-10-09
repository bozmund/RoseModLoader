package net.minecraftforge.event.village;

import java.util.List;
import net.minecraftforge.eventbus.api.Event;

public class WandererTradesEvent extends Event {
    protected List<Object> generic;
    protected List<Object> rare;

    public WandererTradesEvent(List<Object> generic, List<Object> rare) {
        this.generic = generic;
        this.rare = rare;
    }

    public List<Object> getGenericTrades() {
        return generic;
    }

    public List<Object> getRareTrades() {
        return rare;
    }
}
