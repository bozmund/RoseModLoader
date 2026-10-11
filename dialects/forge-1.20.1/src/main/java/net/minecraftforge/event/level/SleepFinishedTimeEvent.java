package net.minecraftforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

/** Everyone slept: the time the level wakes up at; a listener may move it later (not earlier than now). */
public class SleepFinishedTimeEvent extends Event {
    private final ServerLevel level;
    private final long minTime;
    private long newTime;

    public SleepFinishedTimeEvent(ServerLevel level, long newTime, long minTime) {
        this.level = level;
        this.newTime = newTime;
        this.minTime = minTime;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public long getNewTime() {
        return newTime;
    }

    public boolean setTimeAddition(long newTime) {
        if (minTime > newTime) return false;
        this.newTime = newTime;
        return true;
    }
}
