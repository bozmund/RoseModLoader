package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/** Era bridge (1.20.1): sign item. 26.3 uses StandingAndWallBlockItem with {@code Item.Properties.signText()}. */
public class SignItem extends StandingAndWallBlockItem {
    public SignItem(Item.Properties properties, Block standingBlock, Block wallBlock) {
        this(properties, standingBlock, wallBlock, Direction.DOWN);
    }

    public SignItem(Item.Properties properties, Block standingBlock, Block wallBlock, Direction attachmentDirection) {
        super(standingBlock, wallBlock, attachmentDirection, properties.signText());
    }
}
