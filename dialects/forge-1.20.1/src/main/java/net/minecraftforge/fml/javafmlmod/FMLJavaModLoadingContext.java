package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.ForgeModContainer;

/** Gives a mod its own event bus while it is being constructed. */
public class FMLJavaModLoadingContext {
    private final ForgeModContainer container;

    private FMLJavaModLoadingContext(ForgeModContainer container) {
        this.container = container;
    }

    public static FMLJavaModLoadingContext get() {
        ForgeModContainer active = ForgeDialect.activeMod();
        if (active == null) throw new IllegalStateException("FMLJavaModLoadingContext.get() called outside of mod loading");
        return new FMLJavaModLoadingContext(active);
    }

    public IEventBus getModEventBus() {
        return container.modBus();
    }
}
