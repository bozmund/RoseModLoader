package rose.era.v1_20_1.shim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import rose.era.v1_20_1.EraContext;

/**
 * 1.20.1 {@code FriendlyByteBuf} item and text methods. 1.20.5 moved them to registry-aware stream codecs
 * ({@code ItemStack.OPTIONAL_STREAM_CODEC}, {@code ComponentSerialization.STREAM_CODEC}); a plain buffer is wrapped with
 * the game's registries.
 */
public final class FriendlyByteBufShim {
    private FriendlyByteBufShim() {}

    public static FriendlyByteBuf writeItem(FriendlyByteBuf buf, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryAware(buf), stack);
        return buf;
    }

    public static ItemStack readItem(FriendlyByteBuf buf) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(registryAware(buf));
    }

    public static FriendlyByteBuf writeComponent(FriendlyByteBuf buf, Component component) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryAware(buf), component);
        return buf;
    }

    public static Component readComponent(FriendlyByteBuf buf) {
        return ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryAware(buf));
    }

    private static RegistryFriendlyByteBuf registryAware(FriendlyByteBuf buf) {
        return buf instanceof RegistryFriendlyByteBuf r ? r : new RegistryFriendlyByteBuf(buf, EraContext.registryAccess());
    }
}
