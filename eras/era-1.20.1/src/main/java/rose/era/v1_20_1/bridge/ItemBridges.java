package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;

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

    /**
     * 1.20.1 {@code hurtEnemy(stack, target, attacker)} returned whether the item was used (and wore the item itself);
     * 26.3's is void. 1.20.1's ItemStack.hurtEnemy counted a use for players when it returned true; 26.3 counts
     * only items with a {@code weapon} component, which old items don't have, so the use is counted here.
     */
    public static void hurtEnemy(Item self, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var old = Legacy.require(self, "hurtEnemy", MethodType.methodType(boolean.class, ItemStack.class, LivingEntity.class, LivingEntity.class));
        if ((Boolean) Legacy.invoke(old, self, stack, target, attacker)) countUse(self, stack, attacker);
    }

    /** 1.20.1 ItemStack.hurtEnemy's use count, for items that wear themselves (no {@code weapon} component). */
    public static void countUse(Item item, ItemStack stack, LivingEntity attacker) {
        if (attacker instanceof Player player && !stack.has(net.minecraft.core.component.DataComponents.WEAPON)) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(item));
        }
    }

    /**
     * 1.20.1 {@code canAttackBlock(state, level, pos, player)} (26.3 name: canDestroyBlock, which also takes the stack
     * and any living user). Only players asked in 1.20.1, so other users may.
     */
    public static boolean canDestroyBlock(Item self, ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        if (!(user instanceof Player player)) return true;
        var old = Legacy.require(self, "canDestroyBlock", MethodType.methodType(boolean.class, BlockState.class, Level.class, BlockPos.class, Player.class));
        return (Boolean) Legacy.invoke(old, self, state, level, pos, player);
    }

    /**
     * 1.20.1 {@code releaseUsing(stack, level, entity, timeLeft)} returned nothing; 26.3 returns whether to apply the
     * stack's after-use component effects, which 1.20.1 items didn't have.
     */
    public static boolean releaseUsing(Item self, ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        var old = Legacy.require(self, "releaseUsing", MethodType.methodType(void.class, ItemStack.class, Level.class, LivingEntity.class, int.class));
        Legacy.invoke(old, self, stack, level, entity, timeLeft);
        return false;
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
