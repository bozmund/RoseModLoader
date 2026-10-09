package net.minecraftforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

public class BlockEvent extends Event {
    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;

    public BlockEvent(LevelAccessor level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public static class CropGrowEvent extends BlockEvent {
        public CropGrowEvent(LevelAccessor level, BlockPos pos, BlockState state) {
            super(level, pos, state);
        }

        /** Listeners may ALLOW or DENY growth; DEFAULT keeps the crop's own rules. */
        @HasResult
        public static class Pre extends CropGrowEvent {
            public Pre(LevelAccessor level, BlockPos pos, BlockState state) {
                super(level, pos, state);
            }
        }

        public static class Post extends CropGrowEvent {
            private final BlockState originalState;

            public Post(LevelAccessor level, BlockPos pos, BlockState original, BlockState state) {
                super(level, pos, state);
                this.originalState = original;
            }

            public BlockState getOriginalState() {
                return originalState;
            }
        }
    }
}
