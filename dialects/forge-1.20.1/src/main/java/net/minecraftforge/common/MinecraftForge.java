package net.minecraftforge.common;

import net.minecraftforge.eventbus.api.IEventBus;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/** Holds the game-wide ("Forge") event bus; each mod also has its own mod event bus. */
public class MinecraftForge {
    public static final IEventBus EVENT_BUS = new RoseEventBus("forge");

    public static void initialize() {}
}
