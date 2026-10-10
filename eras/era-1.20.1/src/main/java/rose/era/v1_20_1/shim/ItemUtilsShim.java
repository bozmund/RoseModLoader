package rose.era.v1_20_1.shim;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

/** Redirect targets for 1.20.1 {@code ItemUtils}. */
public final class ItemUtilsShim {
    /**
     * 1.20.1 {@code startUsingInstantly} returned {@code consume(the held stack)}; 26.3's returns only the result.
     * Old {@code Item.use} overrides return it as their InteractionResultHolder.
     */
    public static InteractionResultHolder<ItemStack> startUsingInstantly(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(ItemUtils.startUsingInstantly(level, player, hand), player.getItemInHand(hand));
    }

    private ItemUtilsShim() {}
}
