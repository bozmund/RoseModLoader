package rose.dialect.forge.v1_20_1.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/**
 * Forge 1.20.1 RenderPlayerEvent.Pre/Post around submitting a player. The render state carries the player's entity
 * id; the event gets the player from the client level.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class RenderPlayerEventsMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void rose$pre(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Player player = rose$player(state, RenderPlayerEvent.Pre.class);
        if (player != null && MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(player, (AvatarRenderer<?>) (Object) this,
                rose$partialTick(), poseStack, state.lightCoords))) {
            ci.cancel();
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void rose$post(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Player player = rose$player(state, RenderPlayerEvent.Post.class);
        if (player != null) {
            MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Post(player, (AvatarRenderer<?>) (Object) this, rose$partialTick(), poseStack, state.lightCoords));
        }
    }

    /** The player being drawn, if this is a player renderer and someone listens to {@code event}. */
    private Player rose$player(LivingEntityRenderState state, Class<? extends RenderPlayerEvent> event) {
        if (!((Object) this instanceof AvatarRenderer<?>) || !(state instanceof AvatarRenderState avatar)) return null;
        if (!ForgeDialect.active() || !((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(event)) return null;
        var level = Minecraft.getInstance().level;
        return level != null && level.getEntity(avatar.id) instanceof Player player ? player : null;
    }

    private static float rose$partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
