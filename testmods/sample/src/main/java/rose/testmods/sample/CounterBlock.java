package rose.testmods.sample;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import rose.api.network.RoseNetworking;

/** Counts right-clicks in its block entity and tells the clicking player the new count. */
public final class CounterBlock extends Block implements EntityBlock {
    public CounterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CounterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CounterBlockEntity counter) {
            int count = counter.increment();
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                RoseNetworking.sendToPlayer(serverPlayer, new CountPayload(pos, count));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
