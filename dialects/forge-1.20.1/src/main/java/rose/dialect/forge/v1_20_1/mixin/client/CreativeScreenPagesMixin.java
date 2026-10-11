package rose.dialect.forge.v1_20_1.mixin.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.client.CreativeTabPages;

/** Pages of creative tabs (see CreativeTabPages): the tabs of the current page, and buttons to turn pages. */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeScreenPagesMixin {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private void selectTab(CreativeModeTab tab) {
    }

    @Redirect(method = {"mouseClicked", "mouseReleased", "extractRenderState", "extractBackground"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"))
    private List<CreativeModeTab> rose$pageTabs() {
        return CreativeTabPages.visible(CreativeModeTabs.tabs());
    }

    @Inject(method = {"extractRenderState", "extractBackground"}, at = @At("HEAD"))
    private void rose$layoutStart(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        CreativeTabPages.displaying(true);
    }

    @Inject(method = {"extractRenderState", "extractBackground"}, at = @At("RETURN"))
    private void rose$layoutEnd(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        CreativeTabPages.displaying(false);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void rose$clickStart(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        CreativeTabPages.displaying(true);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void rose$clickEnd(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        CreativeTabPages.displaying(false);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void rose$releaseStart(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        CreativeTabPages.displaying(true);
    }

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    private void rose$releaseEnd(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        CreativeTabPages.displaying(false);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void rose$pageButtons(CallbackInfo ci) {
        if (CreativeTabPages.pages() <= 1) return;
        var screen = (CreativeModeInventoryScreen) (Object) this;
        var accessor = (ScreenAccessor) screen;
        int left = ((AbstractContainerScreenAccessor) screen).rose$leftPos();
        int top = ((AbstractContainerScreenAccessor) screen).rose$topPos();
        accessor.rose$addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1)).bounds(left, top - 50, 20, 20).build());
        accessor.rose$addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1)).bounds(left + 175, top - 50, 20, 20).build());
    }

    private void turn(int delta) {
        CreativeTabPages.turn(delta);
        if (CreativeTabPages.pageOf(selectedTab) == -1) return;
        for (CreativeModeTab tab : CreativeTabPages.visible(CreativeModeTabs.tabs())) {
            if (tab.getType() == CreativeModeTab.Type.CATEGORY) {
                selectTab(tab);
                return;
            }
        }
    }
}
