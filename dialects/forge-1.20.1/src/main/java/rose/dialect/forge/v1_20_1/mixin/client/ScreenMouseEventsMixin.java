package rose.dialect.forge.v1_20_1.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/** Forge 1.20.1 ScreenEvent.MouseButtonPressed around a screen handling a click; canceling Pre counts as handled. */
@Mixin(MouseHandler.class)
public abstract class ScreenMouseEventsMixin {
    @WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
    private boolean rose$mousePressed(Screen screen, MouseButtonEvent event, boolean doubleClick, Operation<Boolean> original) {
        if (!ForgeDialect.active()) return original.call(screen, event, doubleClick);
        RoseEventBus bus = (RoseEventBus) MinecraftForge.EVENT_BUS;
        if (bus.hasListeners(ScreenEvent.MouseButtonPressed.Pre.class)
                && bus.post(new ScreenEvent.MouseButtonPressed.Pre(screen, event.x(), event.y(), event.button()))) return true;
        boolean handled = original.call(screen, event, doubleClick);
        if (bus.hasListeners(ScreenEvent.MouseButtonPressed.Post.class)) {
            bus.post(new ScreenEvent.MouseButtonPressed.Post(screen, event.x(), event.y(), event.button(), handled));
        }
        return handled;
    }
}
