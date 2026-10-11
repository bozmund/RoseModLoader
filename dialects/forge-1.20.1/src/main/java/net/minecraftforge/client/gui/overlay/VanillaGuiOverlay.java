package net.minecraftforge.client.gui.overlay;

import net.minecraft.resources.Identifier;
import rose.dialect.forge.v1_20_1.client.HudOverlays;

/** Forge 1.20.1's vanilla HUD elements, in drawing order; mods place their overlays above or below them. */
public enum VanillaGuiOverlay {
    VIGNETTE("vignette"),
    SPYGLASS("spyglass"),
    HELMET("helmet"),
    FROSTBITE("frostbite"),
    PORTAL("portal"),
    HOTBAR("hotbar"),
    CROSSHAIR("crosshair"),
    BOSS_EVENT_PROGRESS("boss_event_progress"),
    PLAYER_HEALTH("player_health"),
    ARMOR_LEVEL("armor_level"),
    FOOD_LEVEL("food_level"),
    AIR_LEVEL("air_level"),
    MOUNT_HEALTH("mount_health"),
    JUMP_BAR("jump_bar"),
    EXPERIENCE_BAR("experience_bar"),
    ITEM_NAME("item_name"),
    SLEEP_FADE("sleep_fade"),
    POTION_ICONS("potion_icons"),
    DEBUG_TEXT("debug_text"),
    FPS_GRAPH("fps_graph"),
    RECORD_OVERLAY("record_overlay"),
    TITLE_TEXT("title_text"),
    SUBTITLES("subtitles"),
    SCOREBOARD("scoreboard"),
    CHAT_PANEL("chat_panel"),
    PLAYER_LIST("player_list");

    public final Identifier id;

    VanillaGuiOverlay(String id) {
        this.id = Identifier.withDefaultNamespace(id);
    }

    public Identifier id() {
        return id;
    }

    public NamedGuiOverlay type() {
        return HudOverlays.find(id);
    }
}
