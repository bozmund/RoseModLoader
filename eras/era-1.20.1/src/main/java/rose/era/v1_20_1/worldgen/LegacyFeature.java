package rose.era.v1_20_1.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * 1.20.1 {@code Feature<FC>}: a feature type with a configuration codec and {@code place(context)}. 26.3's
 * {@code Feature} is the configured feature itself (an interface) and its type is a MapCodec; mod features are
 * rebased onto this class, which registers as that MapCodec and places through {@link LegacyConfiguredFeature}.
 */
public abstract class LegacyFeature<FC extends FeatureConfiguration> {
    private final MapCodec<LegacyConfiguredFeature<FC>> typeCodec;

    public LegacyFeature(Codec<FC> configCodec) {
        // 1.20.1 configured features were {"type": ..., "config": {...}}; packfix keeps that shape for mod types.
        this.typeCodec = configCodec.fieldOf("config").xmap(config -> new LegacyConfiguredFeature<>(this, config), LegacyConfiguredFeature::config);
    }

    public abstract boolean place(FeaturePlaceContext<FC> context);

    /** The value registered in 26.3's FEATURE_TYPE registry (one instance: the registry looks codecs up by identity). */
    public MapCodec<LegacyConfiguredFeature<FC>> typeCodec() {
        return typeCodec;
    }

    protected void setBlock(LevelWriter level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    protected static void safeSetBlock(WorldGenLevel level, BlockPos pos, BlockState state, Predicate<BlockState> canReplace) {
        if (canReplace.test(level.getBlockState(pos))) level.setBlock(pos, state, 2);
    }
}
