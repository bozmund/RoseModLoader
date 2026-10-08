package rose.testmods.sample;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CounterBlockEntity extends BlockEntity {
    private int count;

    public CounterBlockEntity(BlockPos pos, BlockState state) {
        super(SampleMod.COUNTER_BLOCK_ENTITY, pos, state);
    }

    public int count() {
        return count;
    }

    /** Adds one (up to the configured maximum) and returns the new count. */
    public int increment() {
        if (count < SampleMod.CONFIG.maxCount) {
            count++;
            setChanged();
        }
        return count;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.getIntOr("count", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("count", count);
    }
}
