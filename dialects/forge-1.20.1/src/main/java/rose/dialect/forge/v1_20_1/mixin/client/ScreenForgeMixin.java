package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Forge 1.20.1's public getter on screens (its Screen patch); mods call it on their own subclasses (Curios' screens). */
@Mixin(Screen.class)
public abstract class ScreenForgeMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    public Minecraft getMinecraft() {
        return minecraft;
    }
}
