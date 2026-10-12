package net.minecraftforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Around drawing a player (1.20.1 PlayerRenderer is 26.3's AvatarRenderer). Moving the pose stack in Pre and back in
 * Post moves the whole player. 26.3 has no MultiBufferSource (entities are submitted to a SubmitNodeCollector), so
 * Forge's getMultiBufferSource() isn't offered.
 */
public abstract class RenderPlayerEvent extends PlayerEvent {
    private final AvatarRenderer<?> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final int packedLight;

    protected RenderPlayerEvent(Player player, AvatarRenderer<?> renderer, float partialTick, PoseStack poseStack, int packedLight) {
        super(player);
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.packedLight = packedLight;
    }

    public AvatarRenderer<?> getRenderer() {
        return renderer;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public int getPackedLight() {
        return packedLight;
    }

    /** Before the player is drawn; canceling skips drawing them. */
    @Cancelable
    public static class Pre extends RenderPlayerEvent {
        public Pre(Player player, AvatarRenderer<?> renderer, float partialTick, PoseStack poseStack, int packedLight) {
            super(player, renderer, partialTick, poseStack, packedLight);
        }
    }

    public static class Post extends RenderPlayerEvent {
        public Post(Player player, AvatarRenderer<?> renderer, float partialTick, PoseStack poseStack, int packedLight) {
            super(player, renderer, partialTick, poseStack, packedLight);
        }
    }
}
