package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Era bridge (1.20.1): {@code HangingSignRenderer.HangingSignModel}, gone in 26.x (HangingSignRenderer stayed). Named
 * like the nested class so old mods' references resolve; see SignRenderer for what is (not) drawn.
 */
public class HangingSignRenderer$HangingSignModel extends Model<Object> {
    public final ModelPart plank;

    public HangingSignRenderer$HangingSignModel(ModelPart root) {
        super(root, texture -> null);
        this.plank = root.getChild("plank");
    }

    /** 1.20.1 {@code evaluateVisibleParts(state)}: which chains show depends on the block state. */
    public void evaluateVisibleParts(BlockState state) {
    }
}
