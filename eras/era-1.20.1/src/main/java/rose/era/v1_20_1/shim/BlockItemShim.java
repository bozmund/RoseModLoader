package rose.era.v1_20_1.shim;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Redirect targets for 1.20.1 {@code new BlockItem(...)} and its vanilla subclasses. A 1.20.1 block item was named
 * after its block ({@code BlockItem.getDescriptionId()} returned the block's); since 1.21.2 the name comes from
 * {@code Item.Properties}, and vanilla's {@code Items.registerBlock} asks for {@code useBlockDescriptionPrefix()}.
 */
public final class BlockItemShim {
    public static BlockItem create(Block block, Item.Properties properties) {
        return new BlockItem(block, properties.useBlockDescriptionPrefix());
    }

    public static DoubleHighBlockItem doubleHigh(Block block, Item.Properties properties) {
        return new DoubleHighBlockItem(block, properties.useBlockDescriptionPrefix());
    }

    public static HangingSignItem hangingSign(Block hangingSign, Block wallHangingSign, Item.Properties properties) {
        return new HangingSignItem(hangingSign, wallHangingSign, properties.useBlockDescriptionPrefix());
    }

    private BlockItemShim() {}
}
