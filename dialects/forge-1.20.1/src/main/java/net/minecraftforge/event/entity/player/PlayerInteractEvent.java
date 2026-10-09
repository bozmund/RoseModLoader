package net.minecraftforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;

/** A player interacts with the world. Canceling stops vanilla handling; the cancellation result is returned instead. */
public class PlayerInteractEvent extends PlayerEvent {
    private final InteractionHand hand;
    private final BlockPos pos;
    private final Direction face;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        super(player);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getItemStack() {
        return getEntity().getItemInHand(hand);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getFace() {
        return face;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public LogicalSide getSide() {
        return getLevel().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    @Cancelable
    public static class EntityInteract extends PlayerInteractEvent {
        private final Entity target;

        public EntityInteract(Player player, InteractionHand hand, Entity target) {
            super(player, hand, target.blockPosition(), null);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    @Cancelable
    public static class EntityInteractSpecific extends PlayerInteractEvent {
        private final Entity target;

        public EntityInteractSpecific(Player player, InteractionHand hand, Entity target) {
            super(player, hand, target.blockPosition(), null);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    @Cancelable
    public static class RightClickBlock extends PlayerInteractEvent {
        private Event.Result useBlock = Event.Result.DEFAULT;
        private Event.Result useItem = Event.Result.DEFAULT;
        private final BlockHitResult hitVec;

        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
            super(player, hand, pos, hitVec.getDirection());
            this.hitVec = hitVec;
        }

        public BlockHitResult getHitVec() {
            return hitVec;
        }

        public Event.Result getUseBlock() {
            return useBlock;
        }

        public Event.Result getUseItem() {
            return useItem;
        }

        public void setUseBlock(Event.Result triggerBlock) {
            this.useBlock = triggerBlock;
        }

        public void setUseItem(Event.Result triggerItem) {
            this.useItem = triggerItem;
        }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            if (canceled) {
                useBlock = Event.Result.DENY;
                useItem = Event.Result.DENY;
            }
        }
    }

    @Cancelable
    public static class RightClickItem extends PlayerInteractEvent {
        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand, player.blockPosition(), null);
        }
    }

    @Cancelable
    public static class LeftClickBlock extends PlayerInteractEvent {
        public LeftClickBlock(Player player, BlockPos pos, Direction face) {
            super(player, InteractionHand.MAIN_HAND, pos, face);
        }
    }
}
