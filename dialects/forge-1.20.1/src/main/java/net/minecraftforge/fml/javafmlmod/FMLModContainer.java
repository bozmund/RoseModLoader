package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import rose.dialect.forge.v1_20_1.ForgeModContainer;

/** A Java Forge mod: its container also owns the mod's event bus. */
public class FMLModContainer extends ModContainer {
    private final ForgeModContainer mod;

    public FMLModContainer(ForgeModContainer mod) {
        super(mod.metadata());
        this.mod = mod;
    }

    public IEventBus getEventBus() {
        return mod.modBus();
    }

    /** The mod's {@code @Mod} object, or {@code null} before construction. */
    public Object getMod() {
        return mod.instances().isEmpty() ? null : mod.instances().getFirst();
    }
}
