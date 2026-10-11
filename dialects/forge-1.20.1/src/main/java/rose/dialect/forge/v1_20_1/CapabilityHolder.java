package rose.dialect.forge.v1_20_1;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

/**
 * The capabilities attached to one object (Forge's CapabilityDispatcher): providers in attach order, the first that
 * has a capability answers. Saved providers are written under their key ({@code ForgeCaps}).
 */
public final class CapabilityHolder {
    public static final String SAVE_KEY = "ForgeCaps";
    private static final CapabilityHolder EMPTY = new CapabilityHolder(Map.of(), List.of());

    private final Map<Identifier, ICapabilityProvider> providers;
    private final List<Runnable> invalidationListeners;
    private boolean valid = true;

    private CapabilityHolder(Map<Identifier, ICapabilityProvider> providers, List<Runnable> invalidationListeners) {
        this.providers = providers;
        this.invalidationListeners = invalidationListeners;
    }

    /** Posts AttachCapabilitiesEvent for {@code object}; no event when nobody listens. */
    public static <T> CapabilityHolder gather(Class<T> type, T object, Map<Identifier, ICapabilityProvider> initial) {
        if (!ForgeDialect.active()) return EMPTY;
        RoseEventBus bus = (RoseEventBus) MinecraftForge.EVENT_BUS;
        if (!bus.hasListeners(AttachCapabilitiesEvent.class) && initial.isEmpty()) return EMPTY;
        AttachCapabilitiesEvent<T> event = new AttachCapabilitiesEvent<>(type, object);
        initial.forEach(event::addCapability);
        bus.post(event);
        if (event.getCapabilities().isEmpty()) return EMPTY;
        return new CapabilityHolder(event.getCapabilities(), event.getListeners());
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!valid) return LazyOptional.empty();
        for (ICapabilityProvider provider : providers.values()) {
            LazyOptional<T> found = provider.getCapability(cap, side);
            if (found.isPresent()) return found;
        }
        return LazyOptional.empty();
    }

    public void invalidate() {
        valid = false;
        invalidationListeners.forEach(Runnable::run);
    }

    public void revive() {
        valid = true;
    }

    /** The saved providers' data, or {@code null} if none saves anything. */
    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        providers.forEach((key, provider) -> {
            if (provider instanceof INBTSerializable<?> serializable) {
                Tag tag = serializable.serializeNBT();
                if (tag != null) out.put(key.toString(), tag);
            }
        });
        return out.isEmpty() ? null : out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load(CompoundTag saved) {
        providers.forEach((key, provider) -> {
            Tag tag = saved.get(key.toString());
            if (tag != null && provider instanceof INBTSerializable serializable) serializable.deserializeNBT(tag);
        });
    }
}
