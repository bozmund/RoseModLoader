package rose.dialect.forge.v1_20_1.client;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import rose.era.v1_20_1.client.LegacyItemRenderer;

/**
 * Forge called {@code Item.initializeClient(Consumer<IClientItemExtensions>)} from the Item constructor on clients.
 * 26.3's Item has no such hook, so Rose calls it on every item that declares one once the client starts, and wires
 * the extensions it supports: custom item renderers (see LegacyItemRenderer).
 */
public final class ClientItemExtensions {
    public static void collect() {
        int items = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            Method hook = initializeClient(item.getClass());
            if (hook == null) continue;
            IClientItemExtensions[] found = new IClientItemExtensions[1];
            try {
                hook.invoke(item, (Consumer<IClientItemExtensions>) extensions -> found[0] = extensions);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                LogUtils.getLogger().warn("[rose/forge] {}.initializeClient failed: {}", item.getClass().getName(), e.toString());
                continue;
            }
            if (found[0] == null) continue;
            IClientItemExtensions extensions = found[0];
            LegacyItemRenderer.register(item, extensions::getCustomRenderer);
            items++;
        }
        LogUtils.getLogger().info("[rose/forge] client item extensions from {} item(s)", items);
    }

    private static Method initializeClient(Class<?> type) {
        for (Class<?> c = type; c != null && c != Item.class && c != Object.class; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod("initializeClient", Consumer.class);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException next) {
                // keep looking in the superclass
            }
        }
        return null;
    }

    private ClientItemExtensions() {}
}
