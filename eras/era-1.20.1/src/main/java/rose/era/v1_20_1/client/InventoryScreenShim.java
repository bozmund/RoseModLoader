package rose.era.v1_20_1.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

/** Redirect targets for 1.20.1 InventoryScreen helpers that mods' screens call. */
public final class InventoryScreenShim {
    /**
     * 1.20.1 {@code renderEntityInInventoryFollowsMouse(graphics, x, y, scale, dx, dy, entity)}: the entity's feet at
     * (x, y), turned toward the mouse given relative to it. 1.20.2 draws the entity into a box and takes the mouse
     * itself. 1.20.1's inventory drew at (left + 51, top + 75) with scale 30 where 26.3's box is (left + 26, top + 8)
     * to (left + 75, top + 78); the box is that one, scaled, and the mouse is put where it gives 1.20.1's angles.
     */
    public static void renderEntityInInventoryFollowsMouse(GuiGraphicsExtractor graphics, int x, int y, int scale, float dx, float dy,
                                                           LivingEntity entity) {
        float k = scale / 30.0F;
        int x0 = x - Math.round(25 * k);
        int x1 = x + Math.round(24 * k);
        int y0 = y - Math.round(67 * k);
        int y1 = y + Math.round(3 * k);
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x0, y0, x1, y1, scale, 0.0625F, centerX - dx, centerY - dy, entity);
    }

    private InventoryScreenShim() {}
}
