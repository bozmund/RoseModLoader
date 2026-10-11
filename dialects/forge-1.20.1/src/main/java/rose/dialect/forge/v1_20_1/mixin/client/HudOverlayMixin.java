package rose.dialect.forge.v1_20_1.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.client.HudOverlays;

/**
 * Forge 1.20.1's HUD overlay events around 26.3's health, armor, food and air elements, ForgeGui's layout heights
 * (as Forge's vanilla overlays grew them), and mods' overlays (see HudOverlays).
 */
@Mixin(Hud.class)
public abstract class HudOverlayMixin {
    @Shadow
    private boolean isHidden;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void rose$startFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudOverlays.startFrame();
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void rose$modOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!isHidden) HudOverlays.endFrame(graphics);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void rose$hearts(Hud hud, GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int rowHeight, int heartOffsetIndex,
                             float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, Operation<Void> original) {
        if (HudOverlays.pre(VanillaGuiOverlay.PLAYER_HEALTH.id, graphics)) {
            original.call(hud, graphics, player, xLeft, yLineBase, rowHeight, heartOffsetIndex, maxHealth, currentHealth, oldHealth, absorption, blink);
        }
        int rows = Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        HudOverlays.leftHeight += rows * rowHeight + (rowHeight != 10 ? 10 - rowHeight : 0);
        HudOverlays.post(VanillaGuiOverlay.PLAYER_HEALTH.id, graphics);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void rose$armor(GuiGraphicsExtractor graphics, Player player, int yLineBase, int rows, int rowHeight, int xLeft, Operation<Void> original) {
        if (HudOverlays.pre(VanillaGuiOverlay.ARMOR_LEVEL.id, graphics)) original.call(graphics, player, yLineBase, rows, rowHeight, xLeft);
        if (player.getArmorValue() > 0) HudOverlays.leftHeight += 10;
        HudOverlays.post(VanillaGuiOverlay.ARMOR_LEVEL.id, graphics);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void rose$food(Hud hud, GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        if (HudOverlays.pre(VanillaGuiOverlay.FOOD_LEVEL.id, graphics)) original.call(hud, graphics, player, yLineBase, xRight);
        HudOverlays.rightHeight += 10;
        HudOverlays.post(VanillaGuiOverlay.FOOD_LEVEL.id, graphics);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
    private void rose$air(Hud hud, GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, Operation<Void> original) {
        if (HudOverlays.pre(VanillaGuiOverlay.AIR_LEVEL.id, graphics)) original.call(hud, graphics, player, vehicleHearts, yLineAir, xRight);
        if (player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply()) HudOverlays.rightHeight += 10;
        HudOverlays.post(VanillaGuiOverlay.AIR_LEVEL.id, graphics);
    }
}
