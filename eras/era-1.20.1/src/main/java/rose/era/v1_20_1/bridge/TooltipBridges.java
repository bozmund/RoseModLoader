package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Bridges from 26.3 ClientTooltipComponent methods to mods' 1.20.1 overrides. */
public final class TooltipBridges {
    /** 1.20.1 {@code getHeight()}; 26.3 passes the font. */
    public static int getHeight(ClientTooltipComponent self, Font font) {
        return (Integer) Legacy.invoke(Legacy.require(self, "getHeight", MethodType.methodType(int.class)), self);
    }

    /** 1.20.1 {@code renderImage(font, x, y, graphics)}; 26.3 {@code extractImage} also passes the tooltip's size. */
    public static void extractImage(ClientTooltipComponent self, Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        Legacy.invoke(Legacy.require(self, "renderImage", MethodType.methodType(void.class, Font.class, int.class, int.class, GuiGraphicsExtractor.class)),
                self, font, x, y, graphics);
    }

    private TooltipBridges() {}
}
