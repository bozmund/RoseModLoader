package net.minecraft.client.renderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Era bridge (1.20.1): which chunk layer a block draws in. 26.3 picks the layer from each texture's transparency
 * (ChunkSectionLayer.byTransparency), so a mod's choice changes nothing; it is kept only for mods that read it back.
 * The setRenderLayer methods are Forge 1.20.1's additions.
 */
public class ItemBlockRenderTypes {
    private static final Map<Block, RenderType> TYPE_BY_BLOCK = new ConcurrentHashMap<>();
    private static final Map<Fluid, RenderType> TYPE_BY_FLUID = new ConcurrentHashMap<>();
    private static boolean fancy = true;

    public static RenderType getChunkRenderType(BlockState state) {
        return TYPE_BY_BLOCK.getOrDefault(state.getBlock(), RenderTypes.solidMovingBlock());
    }

    public static RenderType getMovingBlockRenderType(BlockState state) {
        return getChunkRenderType(state);
    }

    public static RenderType getRenderType(BlockState state, boolean cull) {
        return getChunkRenderType(state);
    }

    public static RenderType getRenderType(ItemStack stack, boolean cull) {
        return RenderTypes.translucentMovingBlock();
    }

    public static RenderType getRenderLayer(FluidState state) {
        return TYPE_BY_FLUID.getOrDefault(state.getType(), RenderTypes.solidMovingBlock());
    }

    public static void setFancy(boolean fancy) {
        ItemBlockRenderTypes.fancy = fancy;
    }

    public static boolean isFancy() {
        return fancy;
    }

    public static void setRenderLayer(Block block, RenderType type) {
        TYPE_BY_BLOCK.put(block, type);
    }

    public static void setRenderLayer(Block block, Predicate<RenderType> predicate) {
        for (RenderType type : new RenderType[] {RenderTypes.solidMovingBlock(), RenderTypes.cutoutMovingBlock(), RenderTypes.translucentMovingBlock()}) {
            if (predicate.test(type)) {
                TYPE_BY_BLOCK.put(block, type);
                return;
            }
        }
    }

    public static void setRenderLayer(Fluid fluid, RenderType type) {
        TYPE_BY_FLUID.put(fluid, type);
    }
}
