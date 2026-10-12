package rose.era.v1_20_1.shim;

import java.lang.invoke.MethodType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import rose.era.v1_20_1.bridge.Legacy;

/**
 * 1.20.1 calls of BonemealableBlock's methods, which 26.3 gave a BonemealSource (bone meal is INTERACTION, the only
 * source 1.20.1 had). Calls on the interface dispatch as before (a 1.20.1 block's methods are reached through its
 * bridges); {@code super} calls into a game block run that block's own 26.3 implementation.
 */
public final class BonemealShim {
    private static final BonemealSource SOURCE = BonemealSource.INTERACTION;

    public static boolean isValidBonemealTarget(BonemealableBlock self, LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return self.isValidBonemealTarget(level, pos, state, SOURCE);
    }

    public static boolean isBonemealSuccess(BonemealableBlock self, Level level, RandomSource random, BlockPos pos, BlockState state) {
        return self.isBonemealSuccess(level, random, pos, state, SOURCE);
    }

    public static void performBonemeal(BonemealableBlock self, ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        self.performBonemeal(level, random, pos, state, SOURCE);
    }

    public static boolean superIsValidBonemealTarget(Block self, LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        var game = Legacy.superMethod(self, "isValidBonemealTarget", MethodType.methodType(boolean.class, LevelReader.class, BlockPos.class,
                BlockState.class, BonemealSource.class));
        return (Boolean) Legacy.invoke(game, self, level, pos, state, SOURCE);
    }

    public static boolean superIsBonemealSuccess(Block self, Level level, RandomSource random, BlockPos pos, BlockState state) {
        var game = Legacy.superMethod(self, "isBonemealSuccess", MethodType.methodType(boolean.class, Level.class, RandomSource.class,
                BlockPos.class, BlockState.class, BonemealSource.class));
        return (Boolean) Legacy.invoke(game, self, level, random, pos, state, SOURCE);
    }

    public static void superPerformBonemeal(Block self, ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        var game = Legacy.superMethod(self, "performBonemeal", MethodType.methodType(void.class, ServerLevel.class, RandomSource.class,
                BlockPos.class, BlockState.class, BonemealSource.class));
        Legacy.invoke(game, self, level, random, pos, state, SOURCE);
    }

    private BonemealShim() {}
}
