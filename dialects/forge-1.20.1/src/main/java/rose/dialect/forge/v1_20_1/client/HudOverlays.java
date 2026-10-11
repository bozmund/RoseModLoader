package rose.dialect.forge.v1_20_1.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import org.jspecify.annotations.Nullable;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/**
 * Forge 1.20.1's HUD overlays on 26.3's Hud (HudOverlayMixin): RenderGuiOverlayEvent around the vanilla elements
 * Rose hooks (health, armor, food, air), mods' registered overlays next to the element they were registered against
 * (at the end of the HUD for the others), and ForgeGui's leftHeight/rightHeight layout.
 */
public final class HudOverlays {
    private static final Map<Identifier, NamedGuiOverlay> BY_ID = new LinkedHashMap<>();
    /** Mod overlays drawn before ({@code false}) or after ({@code true}) a vanilla element, by its id; null: the HUD. */
    private static final Map<Identifier, List<NamedGuiOverlay>> BELOW = new LinkedHashMap<>();
    private static final Map<Identifier, List<NamedGuiOverlay>> ABOVE = new LinkedHashMap<>();
    private static final Identifier HUD = Identifier.fromNamespaceAndPath("rose", "hud");
    private static final List<Identifier> HOOKED = List.of(VanillaGuiOverlay.PLAYER_HEALTH.id, VanillaGuiOverlay.ARMOR_LEVEL.id,
            VanillaGuiOverlay.FOOD_LEVEL.id, VanillaGuiOverlay.AIR_LEVEL.id);

    public static int leftHeight = 39;
    public static int rightHeight = 39;

    static {
        for (VanillaGuiOverlay vanilla : VanillaGuiOverlay.values()) BY_ID.put(vanilla.id, new NamedGuiOverlay(vanilla.id, null));
    }

    public static synchronized @Nullable NamedGuiOverlay find(Identifier id) {
        return BY_ID.get(id);
    }

    public static synchronized List<NamedGuiOverlay> all() {
        return List.copyOf(BY_ID.values());
    }

    /** RegisterGuiOverlaysEvent: {@code anchor} null means below or above all of the HUD. */
    public static synchronized void register(String path, IGuiOverlay overlay, @Nullable Identifier anchor, boolean above) {
        Identifier id = Identifier.fromNamespaceAndPath(ModLoadingContext.get().getActiveNamespace(), path);
        NamedGuiOverlay named = new NamedGuiOverlay(id, overlay);
        BY_ID.put(id, named);
        Identifier key = anchor != null && HOOKED.contains(anchor) ? anchor : HUD;
        (above || key == HUD ? ABOVE : BELOW).computeIfAbsent(key, k -> new ArrayList<>()).add(named);
    }

    // Redirect targets for mods' ForgeGui.leftHeight / rightHeight reads and writes.

    public static int leftHeight(ForgeGui gui) {
        return leftHeight;
    }

    public static void leftHeight(ForgeGui gui, int value) {
        leftHeight = value;
    }

    public static int rightHeight(ForgeGui gui) {
        return rightHeight;
    }

    public static void rightHeight(ForgeGui gui, int value) {
        rightHeight = value;
    }

    /** A new HUD frame: Forge's layout starts over. */
    public static void startFrame() {
        leftHeight = 39;
        rightHeight = 39;
    }

    /** Before a hooked vanilla element: mods' overlays below it, then Pre; whether to draw the element. */
    public static boolean pre(Identifier id, GuiGraphicsExtractor graphics) {
        if (!ForgeDialect.active()) return true;
        drawAll(BELOW.get(id), graphics);
        if (!hasListeners(RenderGuiOverlayEvent.Pre.class)) return true;
        return !MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(window(), graphics, partialTick(), BY_ID.get(id)));
    }

    /** After a hooked vanilla element: Post, then mods' overlays above it. */
    public static void post(Identifier id, GuiGraphicsExtractor graphics) {
        if (!ForgeDialect.active()) return;
        if (hasListeners(RenderGuiOverlayEvent.Post.class)) {
            MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(window(), graphics, partialTick(), BY_ID.get(id)));
        }
        drawAll(ABOVE.get(id), graphics);
    }

    /** At the end of the HUD: overlays registered against elements Rose doesn't hook. */
    public static void endFrame(GuiGraphicsExtractor graphics) {
        if (ForgeDialect.active()) drawAll(ABOVE.get(HUD), graphics);
    }

    private static void drawAll(@Nullable List<NamedGuiOverlay> overlays, GuiGraphicsExtractor graphics) {
        if (overlays == null) return;
        ForgeGui gui = (ForgeGui) Minecraft.getInstance().gui;
        for (NamedGuiOverlay named : overlays) {
            if (hasListeners(RenderGuiOverlayEvent.Pre.class)
                    && MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(window(), graphics, partialTick(), named))) continue;
            try {
                named.overlay().render(gui, graphics, partialTick(), graphics.guiWidth(), graphics.guiHeight());
            } catch (RuntimeException | LinkageError e) {
                // like a failing event listener: report it and turn it off rather than take the game down
                rose.dialect.forge.v1_20_1.Unsupported.listener("HUD overlay " + named.id(), e);
                synchronized (HudOverlays.class) {
                    overlays.remove(named);
                }
                return;
            }
            if (hasListeners(RenderGuiOverlayEvent.Post.class)) {
                MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(window(), graphics, partialTick(), named));
            }
        }
    }

    private static boolean hasListeners(Class<? extends net.minecraftforge.eventbus.api.Event> event) {
        return ((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(event);
    }

    private static com.mojang.blaze3d.platform.Window window() {
        return Minecraft.getInstance().getWindow();
    }

    private static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    private HudOverlays() {}
}
