package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 1.20.1 {@code BakedModel} as seen by old renderers that asked an item's model about itself. 1.21.4 replaced item
 * models with item model definitions resolved into render states; only these questions are answered.
 */
public interface LegacyBakedModel {
    /** 1.20.1: whether the item model is three-dimensional (block-like); 26.3: whether it uses block lighting. */
    boolean isGui3d();

    /** Forge {@code applyTransform(context, pose, leftHand)}: 26.3 applies display transforms when submitting. */
    default LegacyBakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        return this;
    }

    record Resolved(boolean isGui3d) implements LegacyBakedModel {
    }
}
