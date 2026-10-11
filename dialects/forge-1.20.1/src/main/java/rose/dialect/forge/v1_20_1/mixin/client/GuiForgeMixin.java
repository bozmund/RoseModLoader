package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;

/** Forge made {@code Minecraft.gui} a ForgeGui; mods cast it. */
@Mixin(Gui.class)
public abstract class GuiForgeMixin implements ForgeGui {
}
