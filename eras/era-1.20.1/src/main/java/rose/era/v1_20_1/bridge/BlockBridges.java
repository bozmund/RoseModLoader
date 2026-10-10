package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bridge helpers for blocks (rules/forge-1.20.1/bridges.tsv): 26.3 calls the new method, the helper calls the mod's
 * 1.20.1 override with the arguments it expected.
 */
public final class BlockBridges {
    private static final MethodType USE = MethodType.methodType(InteractionResult.class, BlockState.class, Level.class,
            BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class);

    /** 1.20.1 {@code use} handled both hands, with or without an item; 26.3 split it in two. */
    public static InteractionResult useItemOn(BlockBehaviour self, ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return (InteractionResult) Legacy.invoke(Legacy.require(self, "use", USE), self, state, level, pos, player, hand, hit);
    }

    public static InteractionResult useWithoutItem(BlockBehaviour self, BlockState state, Level level, BlockPos pos, Player player,
                                                   BlockHitResult hit) {
        return (InteractionResult) Legacy.invoke(Legacy.require(self, "use", USE), self, state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    /** 1.20.1 {@code updateShape(state, direction, neighborState, level, pos, neighborPos)}. */
    public static BlockState updateShape(BlockBehaviour self, BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                         Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        var old = Legacy.require(self, "updateShape", MethodType.methodType(BlockState.class, BlockState.class, Direction.class,
                BlockState.class, LevelAccessor.class, BlockPos.class, BlockPos.class));
        if (!(level instanceof LevelAccessor accessor)) return state;
        return (BlockState) Legacy.invoke(old, self, state, direction, neighborState, accessor, pos, neighborPos);
    }

    private static final MethodType UPDATE_SHAPE = MethodType.methodType(BlockState.class, BlockState.class, LevelReader.class,
            ScheduledTickAccess.class, BlockPos.class, Direction.class, BlockPos.class, BlockState.class, RandomSource.class);

    /** 1.20.1 {@code super.updateShape(state, direction, neighborState, level, pos, neighborPos)} from a mod block. */
    public static BlockState superUpdateShape(BlockBehaviour self, BlockState state, Direction direction, BlockState neighborState,
                                              LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return (BlockState) Legacy.invoke(Legacy.superMethod(self, "updateShape", UPDATE_SHAPE),
                self, state, level, level, pos, direction, neighborPos, neighborState, level.getRandom());
    }

    private static final MethodType CLONE_ITEM = MethodType.methodType(ItemStack.class, LevelReader.class, BlockPos.class, BlockState.class, boolean.class);

    /** 1.20.1 {@code super.getCloneItemStack(level, pos, state)} from a mod block (no block entity data). */
    // Redirected calls pass their receiver typed as the class the old code called the method on (Block).
    public static ItemStack superGetCloneItemStack(Block self, BlockGetter level, BlockPos pos, BlockState state) {
        return superGetCloneItemStack((BlockBehaviour) self, level, pos, state);
    }

    public static ItemStack superGetCloneItemStack(BlockBehaviour self, BlockGetter level, BlockPos pos, BlockState state) {
        if (!(level instanceof LevelReader reader)) return new ItemStack(self.asItem());
        return (ItemStack) Legacy.invoke(Legacy.superMethod(self, "getCloneItemStack", CLONE_ITEM), self, reader, pos, state, false);
    }

    /**
     * A mod block's 1.20.1 {@code super.fallOn(level, state, pos, entity, float distance)}: vanilla's own fallOn, which
     * since 1.21.5 takes the distance as a double. Called non-virtually, so the mod's bridged fallOn isn't re-entered.
     */
    public static void superFallOn(Block self, Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        Legacy.invoke(Legacy.superMethod(self, "fallOn", FALL_ON), self, level, state, pos, entity, (double) fallDistance);
    }

    private static final MethodType FALL_ON = MethodType.methodType(void.class, Level.class, BlockState.class, BlockPos.class, Entity.class, double.class);

    /** 1.20.1 {@code onRemove(state, level, pos, newState, isMoving)}: after the block was replaced. */
    public static void affectNeighborsAfterRemoval(BlockBehaviour self, BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        var old = Legacy.require(self, "onRemove", MethodType.methodType(void.class, BlockState.class, Level.class, BlockPos.class,
                BlockState.class, boolean.class));
        Legacy.invoke(old, self, state, level, pos, level.getBlockState(pos), movedByPiston);
    }

    /** 1.20.1 {@code super.onRemove(...)}: removed the block entity, which 26.3 does when the block is replaced. */
    public static void superOnRemove(BlockBehaviour self, BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    }

    public static int getAnalogOutputSignal(BlockBehaviour self, BlockState state, Level level, BlockPos pos, Direction direction) {
        var old = Legacy.require(self, "getAnalogOutputSignal", MethodType.methodType(int.class, BlockState.class, Level.class, BlockPos.class));
        return (Integer) Legacy.invoke(old, self, state, level, pos);
    }

    public static ItemStack getCloneItemStack(BlockBehaviour self, LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        var old = Legacy.require(self, "getCloneItemStack", MethodType.methodType(ItemStack.class, BlockGetter.class, BlockPos.class, BlockState.class));
        return (ItemStack) Legacy.invoke(old, self, level, pos, state);
    }

    /** 1.20.1 {@code isPathfindable(state, level, pos, type)}; 26.3 no longer passes the position. */
    public static boolean isPathfindable(BlockBehaviour self, BlockState state, PathComputationType type) {
        var old = Legacy.require(self, "isPathfindable", MethodType.methodType(boolean.class, BlockState.class, BlockGetter.class,
                BlockPos.class, PathComputationType.class));
        return (Boolean) Legacy.invoke(old, self, state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, type);
    }

    public static VoxelShape getOcclusionShape(BlockBehaviour self, BlockState state) {
        var old = Legacy.require(self, "getOcclusionShape", MethodType.methodType(VoxelShape.class, BlockState.class, BlockGetter.class, BlockPos.class));
        return (VoxelShape) Legacy.invoke(old, self, state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    /** 1.20.1 {@code fallOn(level, state, pos, entity, float fallDistance)}; 26.3 uses a double. */
    public static void fallOn(net.minecraft.world.level.block.Block self, Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        var old = Legacy.require(self, "fallOn", MethodType.methodType(void.class, Level.class, BlockState.class, BlockPos.class, Entity.class, float.class));
        Legacy.invoke(old, self, level, state, pos, entity, (float) fallDistance);
    }

    public static boolean isValidBonemealTarget(BonemealableBlock self, LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        var old = Legacy.require(self, "isValidBonemealTarget", MethodType.methodType(boolean.class, LevelReader.class, BlockPos.class,
                BlockState.class, boolean.class));
        return (Boolean) Legacy.invoke(old, self, level, pos, state, level.isClientSide());
    }

    public static boolean isBonemealSuccess(BonemealableBlock self, Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        var old = Legacy.require(self, "isBonemealSuccess", MethodType.methodType(boolean.class, Level.class, RandomSource.class,
                BlockPos.class, BlockState.class));
        return (Boolean) Legacy.invoke(old, self, level, random, pos, state);
    }

    public static void performBonemeal(BonemealableBlock self, ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        var old = Legacy.require(self, "performBonemeal", MethodType.methodType(void.class, ServerLevel.class, RandomSource.class,
                BlockPos.class, BlockState.class));
        Legacy.invoke(old, self, level, random, pos, state);
    }

    public static boolean canPlaceLiquid(LiquidBlockContainer self, LivingEntity owner, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        var old = Legacy.require(self, "canPlaceLiquid", MethodType.methodType(boolean.class, BlockGetter.class, BlockPos.class,
                BlockState.class, Fluid.class));
        return (Boolean) Legacy.invoke(old, self, level, pos, state, fluid);
    }

    /** 1.20.1 {@code InteractionResult.sidedSuccess(isClient)}: 26.3 has one SUCCESS that predicts the swing client-side. */
    public static InteractionResult sidedSuccess(boolean isClientSide) {
        return InteractionResult.SUCCESS;
    }

    private BlockBridges() {}
}
