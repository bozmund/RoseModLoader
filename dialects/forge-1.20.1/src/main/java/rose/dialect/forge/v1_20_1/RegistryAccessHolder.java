package rose.dialect.forge.v1_20_1;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;

/**
 * Old Forge code saved item stacks to NBT without a registry context; 26.3 needs one (item components can refer
 * to data-driven registries such as enchantments). This supplies the running server's registries, or the
 * built-in ones before a world is loaded.
 */
public final class RegistryAccessHolder {
    private static volatile MinecraftServer server;

    /** Set by ServerLifecycleMixin. */
    public static void setServer(MinecraftServer value) {
        server = value;
    }

    public static MinecraftServer server() {
        return server;
    }

    public static HolderLookup.Provider provider() {
        MinecraftServer s = server;
        if (s != null) return s.registryAccess();
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static RegistryOps<Tag> nbtOps() {
        return provider().createSerializationContext(NbtOps.INSTANCE);
    }

    private RegistryAccessHolder() {}
}
