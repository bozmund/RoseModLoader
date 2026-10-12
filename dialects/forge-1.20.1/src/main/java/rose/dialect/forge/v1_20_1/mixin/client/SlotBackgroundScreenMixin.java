package rose.dialect.forge.v1_20_1.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import java.lang.invoke.MethodType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeSlot;
import rose.era.v1_20_1.bridge.Legacy;

/**
 * The icon of an empty 1.20.1 slot: its {@code getNoItemIcon()} override (a pair of atlas and sprite in 1.20.1), or a
 * Forge background (Slot.setBackground). 26.3 slots name a GUI sprite instead. Vanilla's empty-armor icons are GUI
 * sprites in 26.3 (InventoryMenu.EMPTY_ARMOR_SLOT_* now name {@code container/slot/...}), so those are shown as such;
 * other sprites are drawn from their atlas. 1.20.1 named an atlas by its texture ({@code textures/atlas/blocks.png}),
 * 26.3 by id ({@code blocks}).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class SlotBackgroundScreenMixin {
    @Unique
    private static final MethodType OLD_ICON = MethodType.methodType(Pair.class);

    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getNoItemIcon()Lnet/minecraft/resources/Identifier;"))
    private Identifier rose$legacyIcon(Slot slot, Operation<Identifier> original, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
        Identifier icon = original.call(slot);
        if (icon != null) return icon;
        Identifier atlas = null;
        Identifier sprite = null;
        var old = Legacy.find(slot, "getNoItemIcon", OLD_ICON);
        if (old.isPresent() && Legacy.invoke(old.get(), slot) instanceof Pair<?, ?> pair) {
            atlas = (Identifier) pair.getFirst();
            sprite = (Identifier) pair.getSecond();
        } else if (slot instanceof ForgeSlot forge) {
            atlas = forge.rose$backgroundAtlas();
            sprite = forge.rose$backgroundSprite();
        }
        if (sprite == null) return null;
        if (sprite.getPath().startsWith("container/")) return sprite;
        var texture = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlasId(atlas));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture.getSprite(sprite), slot.x, slot.y, 16, 16);
        return null;
    }

    /** {@code ns:textures/atlas/name.png} (1.20.1) to the atlas id {@code ns:name}; no atlas means the block atlas. */
    @Unique
    private static Identifier atlasId(Identifier legacy) {
        if (legacy == null) return Identifier.withDefaultNamespace("blocks");
        String path = legacy.getPath();
        if (path.startsWith("textures/atlas/") && path.endsWith(".png")) {
            return Identifier.fromNamespaceAndPath(legacy.getNamespace(), path.substring("textures/atlas/".length(), path.length() - 4));
        }
        return legacy;
    }
}
