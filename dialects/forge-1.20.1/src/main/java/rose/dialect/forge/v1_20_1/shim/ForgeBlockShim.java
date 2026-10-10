package rose.dialect.forge.v1_20_1.shim;

import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import rose.era.v1_20_1.bridge.Legacy;

/**
 * Redirect targets for Forge 1.20.1's block, block state and level extensions used by farming code (IForgeBlock,
 * IForgeBlockState, Forge's LevelReader patch). A mod block's own override of the IForgeBlock method wins; otherwise
 * Forge's default applies, as written in Forge 1.20.1 (forge-1.20.1-47.4.26: Block.java.patch, IForgeBlock).
 */
public final class ForgeBlockShim {
    /** Forge {@code LevelReader.isAreaLoaded(center, range)}: the chunks around {@code center} are loaded. */
    public static boolean isAreaLoaded(LevelReader self, BlockPos center, int range) {
        return self.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }

    // Redirected calls pass their receiver typed as the class the old code called the method on.
    public static boolean isAreaLoaded(Level self, BlockPos center, int range) {
        return isAreaLoaded((LevelReader) self, center, range);
    }

    public static boolean isAreaLoaded(ServerLevel self, BlockPos center, int range) {
        return isAreaLoaded((LevelReader) self, center, range);
    }

    /** Forge {@code BlockState.canBeHydrated(level, pos, fluid, fluidPos)}; by default water hydrates (FluidType.canHydrate). */
    public static boolean canBeHydrated(BlockState self, BlockGetter level, BlockPos pos, FluidState fluid, BlockPos fluidPos) {
        Method override = override(self.getBlock(), "canBeHydrated", BlockState.class, BlockGetter.class, BlockPos.class, FluidState.class, BlockPos.class);
        if (override != null) return (Boolean) call(override, self.getBlock(), self, level, pos, fluid, fluidPos);
        return fluid.is(FluidTags.WATER);
    }

    /** Forge {@code BlockState.isFertile(level, pos)}; by default moist farmland is fertile. */
    public static boolean isFertile(BlockState self, BlockGetter level, BlockPos pos) {
        Method override = override(self.getBlock(), "isFertile", BlockState.class, BlockGetter.class, BlockPos.class);
        if (override != null) return (Boolean) call(override, self.getBlock(), self, level, pos);
        return self.is(Blocks.FARMLAND) && self.getValue(FarmlandBlock.MOISTURE) > 0;
    }

    private static final ClassValue<Method> CAN_SUSTAIN_PLANT = new ClassValue<>() {
        @Override
        protected Method computeValue(Class<?> type) {
            try {
                return type.getMethod("canSustainPlant", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class, IPlantable.class);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    };

    /** Whether a (mod) block decides itself which plants grow on it: it overrides Forge's canSustainPlant. */
    public static boolean decidesPlants(Block block) {
        return CAN_SUSTAIN_PLANT.get(block.getClass()) != null;
    }

    /** Forge {@code BlockState.canSustainPlant(level, pos, facing, plant)}: whether the plant can grow on this block. */
    public static boolean canSustainPlant(BlockState self, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        Block block = self.getBlock();
        Method override = CAN_SUSTAIN_PLANT.get(block.getClass());
        if (override != null) return (Boolean) call(override, block, self, level, pos, facing, plantable);

        BlockPos plantPos = pos.relative(facing);
        BlockState plant = plantable.getPlant(level, plantPos);
        PlantType type = plantable.getPlantType(level, plantPos);
        if (plant.getBlock() == Blocks.CACTUS) return self.is(Blocks.CACTUS) || self.is(BlockTags.SAND);
        if (plant.getBlock() == Blocks.SUGAR_CANE && block == Blocks.SUGAR_CANE) return true;
        // 1.20.1 BushBlock (the vegetation base class) is VegetationBlock on 26.3.
        if (plantable instanceof VegetationBlock vegetation && mayPlaceOn(vegetation, self, level, pos)) return true;

        if (PlantType.DESERT.equals(type)) return self.is(BlockTags.SAND) || block == Blocks.TERRACOTTA || block instanceof GlazedTerracottaBlock;
        if (PlantType.NETHER.equals(type)) return block == Blocks.SOUL_SAND;
        if (PlantType.CROP.equals(type)) return self.is(Blocks.FARMLAND);
        if (PlantType.CAVE.equals(type)) return self.isFaceSturdy(level, pos, Direction.UP);
        if (PlantType.PLAINS.equals(type)) return self.is(BlockTags.DIRT) || block == Blocks.FARMLAND;
        if (PlantType.WATER.equals(type)) {
            return (self.is(Blocks.WATER) || block instanceof IceBlock) && level.getFluidState(plantPos).isEmpty();
        }
        if (PlantType.BEACH.equals(type)) {
            if (!self.is(BlockTags.DIRT) && !self.is(BlockTags.SAND)) return false;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(face);
                if (level.getBlockState(side).is(Blocks.FROSTED_ICE) || level.getFluidState(side).is(FluidTags.WATER)) return true;
            }
            return false;
        }
        return false;
    }

    private static boolean mayPlaceOn(VegetationBlock vegetation, BlockState ground, BlockGetter level, BlockPos pos) {
        var handle = Legacy.require(vegetation, "mayPlaceOn", MethodType.methodType(boolean.class, BlockState.class, BlockGetter.class, BlockPos.class));
        return (Boolean) Legacy.invoke(handle, vegetation, ground, level, pos);
    }

    /** The block's own public method (a mod's IForgeBlock override), or {@code null}: 26.3's Block has none of these. */
    private static Method override(Block block, String name, Class<?>... parameters) {
        try {
            return block.getClass().getMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Object call(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private ForgeBlockShim() {}
}
