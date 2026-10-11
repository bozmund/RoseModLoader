package rose.dialect.forge.v1_20_1.mixin.client;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;
import rose.dialect.forge.v1_20_1.client.TooltipFactories;
import rose.dialect.forge.v1_20_1.client.TooltipItem;

/**
 * Forge 1.20.1 RenderTooltipEvent.GatherComponents for item tooltips: the lines and image of the hovered stack
 * (TooltipItem) as Forge's tooltip elements, which listeners change before they become drawable components. Mod
 * tooltip images get the client component their mod's factory makes (26.3's create knows vanilla's only).
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class TooltipComponentsMixin {
    @Shadow
    public abstract int guiWidth();

    @Shadow
    public abstract int guiHeight();

    @Shadow
    private void setTooltipForNextFrameInternal(Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner,
                                                @Nullable Identifier style, boolean replaceExisting, boolean extraSpaceAfterFirstLine) {
    }

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void rose$itemTooltip(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipItem.set(stack);
    }

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
    private void rose$itemTooltipDone(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipItem.set(null);
    }

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void rose$gather(Font font, List<Component> texts, Optional<TooltipComponent> image, int x, int y, @Nullable Identifier style,
                             boolean extraSpaceAfterFirstLine, CallbackInfo ci) {
        if (!ForgeDialect.active()) return;
        ItemStack stack = TooltipItem.get();
        boolean gather = stack != null && ((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(RenderTooltipEvent.GatherComponents.class);
        boolean modImage = image.isPresent() && TooltipFactories.create(image.get()) != null;
        if (!gather && !modImage) return;
        List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
        for (Component text : texts) elements.add(Either.left(text));
        image.ifPresent(i -> elements.add(elements.isEmpty() ? 0 : 1, Either.right(i)));
        if (gather) {
            var event = new RenderTooltipEvent.GatherComponents(stack, guiWidth(), guiHeight(), elements, -1);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                ci.cancel();
                return;
            }
        }
        List<ClientTooltipComponent> lines = new ArrayList<>();
        for (var element : elements) {
            lines.add(element.map(text -> ClientTooltipComponent.create(Language.getInstance().getVisualOrder(text)), TooltipComponentsMixin::component));
        }
        setTooltipForNextFrameInternal(font, lines, x, y, DefaultTooltipPositioner.INSTANCE, style, false, extraSpaceAfterFirstLine);
        ci.cancel();
    }

    /** A mod's tooltip image through the factory it registered (RegisterClientTooltipComponentFactoriesEvent). */
    private static ClientTooltipComponent component(TooltipComponent image) {
        ClientTooltipComponent mod = TooltipFactories.create(image);
        return mod != null ? mod : ClientTooltipComponent.create(image);
    }
}
