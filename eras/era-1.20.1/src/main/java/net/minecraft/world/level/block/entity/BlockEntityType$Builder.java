package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.types.Type;
import java.util.Set;
import net.minecraft.world.level.block.Block;

/**
 * Era bridge (1.20.1): {@code BlockEntityType.Builder.of(factory, blocks).build(null)}. Removed in 1.21.2; 26.3
 * constructs BlockEntityType directly. (Declared as a top-level class named like the old nested one.)
 */
public final class BlockEntityType$Builder<T extends BlockEntity> {
    private final BlockEntityType.BlockEntitySupplier<? extends T> factory;
    private final Set<Block> validBlocks;

    private BlockEntityType$Builder(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks) {
        this.factory = factory;
        this.validBlocks = validBlocks;
    }

    public static <T extends BlockEntity> BlockEntityType$Builder<T> of(BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... validBlocks) {
        return new BlockEntityType$Builder<>(factory, Set.of(validBlocks));
    }

    public BlockEntityType<T> build(Type<?> dataType) {
        return new BlockEntityType<>(factory, validBlocks);
    }
}
