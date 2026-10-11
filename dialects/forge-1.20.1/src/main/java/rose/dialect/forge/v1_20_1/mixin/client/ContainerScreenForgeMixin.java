package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Forge 1.20.1's public getters on container screens (its AbstractContainerScreen patch). Mods call them on their own
 * subclasses (Curios' CuriosScreen), so they have to be members of the class, not redirects.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenForgeMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    @Final
    protected int imageWidth;

    @Shadow
    @Final
    protected int imageHeight;

    @Shadow
    protected @Nullable Slot hoveredSlot;

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }

    public int getXSize() {
        return imageWidth;
    }

    public int getYSize() {
        return imageHeight;
    }

    public @Nullable Slot getSlotUnderMouse() {
        return hoveredSlot;
    }
}
