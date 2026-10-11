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

    /** A player is about to break a block (server); canceling keeps it. */
    @net.minecraftforge.eventbus.api.Cancelable
    public static class BreakEvent extends BlockEvent {
        private final net.minecraft.world.entity.player.Player player;
        private int exp;

        public BreakEvent(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
            super(level, pos, state);
            this.player = player;
            // as Forge: what the block drops for the held tool (none if it can't harvest the block)
            if (state != null && (!state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state))) {
                var tool = player.getMainHandItem();
                this.exp = rose.era.v1_20_1.shim.BlockStateShim.getExpDrop(state, level, level.getRandom(), pos,
                        level(level, tool, net.minecraft.world.item.enchantment.Enchantments.FORTUNE),
                        level(level, tool, net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH));
            }
        }

        private static int level(net.minecraft.world.level.Level level, net.minecraft.world.item.ItemStack tool,
                                 net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
            return level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(enchantment)
                    .map(h -> net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(h, tool)).orElse(0);
        }

        public net.minecraft.world.entity.player.Player getPlayer() {
            return player;
        }

        /** Experience the break drops, as Forge computed it; changing it doesn't change 26.3's own drops yet. */
        public int getExpToDrop() {
            return exp;
        }

        public void setExpToDrop(int exp) {
            this.exp = exp;
        }
    }
}
