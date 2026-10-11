package rose.era.v1_20_1.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 1.20.1's HUD sheet {@code textures/gui/icons.png}, gone since 1.20.2 split it into {@code hud/*} GUI sprites. Mods
 * still blit regions of it (AppleSkin's food tooltip); each region is drawn from the sprite that replaced it. The
 * regions are where 1.20.1's Gui drew them from (renderHearts/HeartType.getX, renderPlayerHealth, renderCrosshair,
 * renderHotbar, renderExperienceBar, renderJumpMeter).
 */
public final class LegacyIcons {
    public static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/icons.png");

    private record Region(int u, int v, int width, int height, Identifier sprite) {
        boolean contains(float u, float v) {
            return u >= this.u && u < this.u + width && v >= this.v && v < this.v + height;
        }
    }

    private static final List<Region> REGIONS = new ArrayList<>();

    static {
        region(0, 0, 15, 15, "crosshair");
        hearts(0, "");
        hearts(45, "hardcore_");
        icons(9, 16, "armor_empty", "armor_half", "armor_full");
        region(52, 9, 9, 9, "heart/vehicle_container");
        region(88, 9, 9, 9, "heart/vehicle_full");
        region(97, 9, 9, 9, "heart/vehicle_half");
        icons(18, 16, "air", "air_bursting");
        region(16, 27, 9, 9, "food_empty");
        region(52, 27, 9, 9, "food_full");
        region(61, 27, 9, 9, "food_half");
        region(88, 27, 9, 9, "food_full_hunger");
        region(97, 27, 9, 9, "food_half_hunger");
        region(133, 27, 9, 9, "food_empty_hunger");
        region(0, 64, 182, 5, "experience_bar_background");
        region(0, 69, 182, 5, "experience_bar_progress");
        region(0, 74, 182, 5, "jump_bar_cooldown");
        region(0, 84, 182, 5, "jump_bar_background");
        region(0, 89, 182, 5, "jump_bar_progress");
        region(0, 94, 18, 18, "hotbar_attack_indicator_background");
        region(18, 94, 18, 18, "hotbar_attack_indicator_progress");
        region(36, 94, 16, 4, "crosshair_attack_indicator_background");
        region(52, 94, 16, 4, "crosshair_attack_indicator_progress");
        region(68, 94, 16, 16, "crosshair_attack_indicator_full");
    }

    /** A heart row: container at 16, then per type full, half, full blinking, half blinking (9 wide each). */
    private static void hearts(int v, String hardcore) {
        region(16, v, 9, 9, "heart/container" + (hardcore.isEmpty() ? "" : "_hardcore"));
        region(25, v, 9, 9, "heart/container" + (hardcore.isEmpty() ? "" : "_hardcore") + "_blinking");
        String[] types = {"", "poisoned_", "withered_"};
        for (int i = 0; i < types.length; i++) {
            icons(v, 52 + i * 36, "heart/" + types[i] + hardcore + "full", "heart/" + types[i] + hardcore + "half",
                    "heart/" + types[i] + hardcore + "full_blinking", "heart/" + types[i] + hardcore + "half_blinking");
        }
        icons(v, 160, "heart/absorbing_" + hardcore + "full", "heart/absorbing_" + hardcore + "half");
        icons(v, 178, "heart/frozen_" + hardcore + "full", "heart/frozen_" + hardcore + "half");
    }

    private static void icons(int v, int u, String... sprites) {
        for (int i = 0; i < sprites.length; i++) region(u + i * 9, v, 9, 9, sprites[i]);
    }

    private static void region(int u, int v, int width, int height, String sprite) {
        REGIONS.add(new Region(u, v, width, height, Identifier.withDefaultNamespace("hud/" + sprite)));
    }

    /** The sprite drawn for the sheet region at (u, v), or null where the sheet had nothing Rose maps. */
    public static Identifier sprite(float u, float v) {
        for (Region region : REGIONS) {
            if (region.contains(u, v)) return region.sprite();
        }
        return null;
    }

    /** Draws a {@code width x height} piece of the sheet at (u, v); false if the sheet had no sprite there. */
    public static boolean blit(GuiGraphicsExtractor g, int x, int y, float u, float v, int width, int height, int color) {
        for (Region region : REGIONS) {
            if (!region.contains(u, v)) continue;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, region.sprite(), region.width(), region.height(),
                    (int) u - region.u(), (int) v - region.v(), x, y, width, height, color);
            return true;
        }
        return false;
    }

    private LegacyIcons() {}
}
