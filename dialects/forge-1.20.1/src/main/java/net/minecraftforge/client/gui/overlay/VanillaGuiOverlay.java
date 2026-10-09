package net.minecraftforge.client.gui.overlay;

import net.minecraft.resources.Identifier;

public enum VanillaGuiOverlay {
    PLAYER_HEALTH("player_health"),
    FOOD_LEVEL("food_level"),
    ARMOR_LEVEL("armor_level"),
    AIR_LEVEL("air_level"),
    HOTBAR("hotbar");

    public final Identifier id;

    VanillaGuiOverlay(String id) {
        this.id = Identifier.withDefaultNamespace(id);
    }

    public Identifier id() {
        return id;
    }

    public NamedGuiOverlay type() {
        return new NamedGuiOverlay(id);
    }
}
