package rose.dialect.forge.v1_20_1.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/** Forge 1.20.1 CustomizeGuiOverlayEvent.DebugText: the F3 columns, once 26.3 has filled them and before they are drawn. */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugTextMixin {
    @Inject(method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;ZI)V",
                    ordinal = 0))
    private void rose$debugText(GuiGraphicsExtractor graphics, CallbackInfo ci, @Local(ordinal = 0) List<String> left, @Local(ordinal = 1) List<String> right) {
        if (!ForgeDialect.active() || !((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(CustomizeGuiOverlayEvent.DebugText.class)) return;
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.post(new CustomizeGuiOverlayEvent.DebugText(minecraft.getWindow(), graphics,
                minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), (ArrayList<String>) left, (ArrayList<String>) right));
    }
}
