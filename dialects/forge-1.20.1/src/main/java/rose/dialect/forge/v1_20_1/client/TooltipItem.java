package rose.dialect.forge.v1_20_1.client;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** The stack whose tooltip is being set up on this thread (26.3's tooltip calls don't carry it). */
public final class TooltipItem {
    private static final ThreadLocal<ItemStack> CURRENT = new ThreadLocal<>();

    public static void set(@Nullable ItemStack stack) {
        if (stack == null) CURRENT.remove();
        else CURRENT.set(stack);
    }

    public static @Nullable ItemStack get() {
        return CURRENT.get();
    }

    private TooltipItem() {}
}
