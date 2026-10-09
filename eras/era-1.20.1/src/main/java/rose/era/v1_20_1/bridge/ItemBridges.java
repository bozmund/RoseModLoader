package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Bridge helpers for items (rules/forge-1.20.1/bridges.tsv). */
public final class ItemBridges {
    /**
     * 1.20.1 {@code use} returned the result and the stack to hold afterwards; 26.3 returns a result that carries the
     * new stack when it changed.
     */
    public static InteractionResult use(Item self, Level level, Player player, InteractionHand hand) {
        var old = Legacy.require(self, "use", MethodType.methodType(InteractionResultHolder.class, Level.class, Player.class, InteractionHand.class));
        InteractionResultHolder<?> holder = (InteractionResultHolder<?>) Legacy.invoke(old, self, level, player, hand);
        InteractionResult result = holder.getResult();
        if (result instanceof InteractionResult.Success success && holder.getObject() instanceof ItemStack stack
                && stack != player.getItemInHand(hand)) {
            return success.heldItemTransformedTo(stack);
        }
        return result;
    }

    /** 1.20.1 {@code appendHoverText(stack, level, lines, flag)}; 26.3 streams lines to a consumer (no level). */
    public static void appendHoverText(Item self, ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                       Consumer<Component> builder, TooltipFlag flag) {
        var old = Legacy.require(self, "appendHoverText", MethodType.methodType(void.class, ItemStack.class, Level.class, List.class, TooltipFlag.class));
        List<Component> lines = new ArrayList<>();
        Legacy.invoke(old, self, stack, null, lines, flag);
        lines.forEach(builder);
    }

    public static int getUseDuration(Item self, ItemStack stack, LivingEntity user) {
        var old = Legacy.require(self, "getUseDuration", MethodType.methodType(int.class, ItemStack.class));
        return (Integer) Legacy.invoke(old, self, stack);
    }

    private ItemBridges() {}
}
