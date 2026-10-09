package rose.dialect.forge.v1_20_1;

import com.mojang.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import rose.api.ModInitializer;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/**
 * Loads translated Forge 1.20.1 mods the way FML did: construct each {@code @Mod} class with its own mod event bus,
 * register {@code @EventBusSubscriber} classes, fire {@link RegisterEvent} for every registry, then the setup
 * events. Runs as the dialect mod's main entrypoint, just before vanilla freezes its registries.
 */
public final class ForgeDialect implements ModInitializer {
    public static final String DIALECT = "forge-1.20.1";
    // Written by the launcher (rose.boot.forge.ForgeMods) from @Mod and @Mod.EventBusSubscriber annotations.
    static final String MOD_ENTRYPOINT = "forge:mod";
    static final String SUBSCRIBER_ENTRYPOINT = "forge:subscriber";
    private static final Logger LOG = LogUtils.getLogger();
    private static final List<ForgeModContainer> MODS = new ArrayList<>();
    private static ForgeModContainer active;

    /** The mod being constructed or receiving a mod-bus event, for {@code FMLJavaModLoadingContext.get()}. */
    public static ForgeModContainer activeMod() {
        return active;
    }

    public static List<ForgeModContainer> mods() {
        return MODS;
    }

    @Override
    public void onInitialize() {
        for (ModMetadata mod : RoseLoader.get().mods()) {
            if (DIALECT.equals(mod.dialect())) MODS.add(new ForgeModContainer(mod));
        }
        if (MODS.isEmpty()) return;
        MenuData.register();
        net.minecraftforge.common.loot.CanToolPerformAction.register();
        ForgeServerEvents.register();
        LOG.info("[rose/forge] loading {} Forge 1.20.1 mod(s): {}", MODS.size(), MODS.stream().map(ForgeModContainer::modId).toList());
        for (ForgeModContainer mod : MODS) construct(mod);
        for (ResourceKey<? extends Registry<?>> key : registrationOrder()) {
            RegisterEvent event = new RegisterEvent(key, ForgeRegistryLookup.get(key.identifier()));
            for (ForgeModContainer mod : MODS) postToMod(mod, event, "RegisterEvent(" + key.identifier() + ")");
        }
    }

    /** After vanilla froze its registries (BuiltInRegistriesMixin): common setup, and on servers the rest. */
    public static void afterFreeze() {
        if (MODS.isEmpty()) return;
        dispatchLifecycle(FMLCommonSetupEvent::new);
        if (FMLEnvironment.dist.isDedicatedServer()) {
            dispatchLifecycle(FMLDedicatedServerSetupEvent::new);
            dispatchLifecycle(FMLLoadCompleteEvent::new);
        }
    }

    /** Sends one lifecycle event to each mod, then runs the work they queued, one mod at a time. */
    static void dispatchLifecycle(Supplier<? extends ParallelDispatchEvent> factory) {
        for (ForgeModContainer mod : MODS) {
            ParallelDispatchEvent event = factory.get();
            String name = event.getClass().getSimpleName();
            postToMod(mod, event, name);
            for (Runnable work : event.rose$drainWork()) {
                try {
                    work.run();
                } catch (RuntimeException | LinkageError e) {
                    LOG.error("[rose/forge] {}: work queued in {} failed", mod.modId(), name, e);
                    Unsupported.feature("work:" + mod.modId() + ":" + name, mod.modId() + ": work queued in " + name + " failed: " + e);
                }
            }
        }
    }

    private static void construct(ForgeModContainer mod) {
        active = mod;
        try {
            ClassLoader loader = RoseLoader.get().classLoader();
            for (String className : mod.metadata().entrypoints(MOD_ENTRYPOINT)) {
                Class<?> type = Class.forName(className, true, loader);
                Mod annotation = type.getAnnotation(Mod.class);
                if (annotation != null && !annotation.value().equals(mod.modId())) continue; // another mod in the same jar
                try {
                    mod.instances().add(type.getDeclaredConstructor().newInstance());
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Forge mod '" + mod.modId() + "': constructor of " + className + " failed", e.getCause());
                }
            }
            for (String className : mod.metadata().entrypoints(SUBSCRIBER_ENTRYPOINT)) {
                registerSubscriber(mod, Class.forName(className, false, loader));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Forge mod '" + mod.modId() + "' could not be constructed", e);
        } finally {
            active = null;
        }
    }

    private static void registerSubscriber(ForgeModContainer mod, Class<?> type) {
        Mod.EventBusSubscriber sub = type.getAnnotation(Mod.EventBusSubscriber.class);
        if (sub == null) return;
        if (!sub.modid().isEmpty() && !sub.modid().equals(mod.modId())) return;
        if (!Arrays.asList(sub.value()).contains(FMLEnvironment.dist)) return;
        (sub.bus() == Mod.EventBusSubscriber.Bus.MOD ? mod.modBus() : MinecraftForge.EVENT_BUS).register(type);
    }

    private static void postToMod(ForgeModContainer mod, Event event, String what) {
        ForgeModContainer previous = active;
        active = mod;
        try {
            mod.modBus().post(event);
        } catch (RuntimeException | LinkageError e) {
            throw new IllegalStateException("Forge mod '" + mod.modId() + "' failed handling " + what, e);
        } finally {
            active = previous;
        }
    }

    /**
     * Vanilla's registry order, as Forge fired RegisterEvent (sounds and mob effects come before blocks, blocks before
     * items), then registries 26.3 doesn't have built in.
     */
    private static List<ResourceKey<? extends Registry<?>>> registrationOrder() {
        Set<ResourceKey<? extends Registry<?>>> order = new LinkedHashSet<>();
        for (Registry<?> registry : RegistryValues.all(BuiltInRegistries.REGISTRY)) order.add(registry.key());
        // Registries mods asked for that 26.3 doesn't have built in (Forge's own, now data-driven ones).
        for (ForgeRegistry<?> registry : ForgeRegistryLookup.known()) order.add(registry.getRegistryKey());
        return new ArrayList<>(order);
    }
}
