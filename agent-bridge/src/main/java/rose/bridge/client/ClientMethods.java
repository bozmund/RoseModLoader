package rose.bridge.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rose.bridge.BridgeServer;
import rose.bridge.Params;

/** Client-only methods: what's on screen, GUI clicks, screenshots, joining worlds, the local player's actions. */
public final class ClientMethods {
    private static final long TIMEOUT_SECONDS = 30;

    public static void register(BridgeServer bridge) {
        bridge.register("client.status", "", "Current screen, whether in a world, connection kind, and the player's position.",
                p -> onClient(ClientMethods::status));

        bridge.register("client.screen", "", "The open screen: class, title and its widgets (index, type, text, active, bounds). "
                + "Use an index or text with client.click / client.setText.", p -> onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.gui.screen();
            JsonObject out = new JsonObject();
            if (screen == null) {
                out.addProperty("screen", (String) null);
                return out;
            }
            out.addProperty("screen", screen.getClass().getName());
            out.addProperty("title", screen.getTitle().getString());
            JsonArray widgets = new JsonArray();
            List<AbstractWidget> all = widgets(screen);
            for (int i = 0; i < all.size(); i++) widgets.add(describe(i, all.get(i)));
            out.add("widgets", widgets);
            return out;
        }));

        bridge.register("client.click", "index?:int | text?:string (exact, case-insensitive)",
                "Left-clicks a widget on the open screen, at its center, the way a mouse click would.", p -> onClient(() -> {
                    Screen screen = requireScreen();
                    AbstractWidget widget = findWidget(screen, p);
                    double x = widget.getX() + widget.getWidth() / 2.0;
                    double y = widget.getY() + widget.getHeight() / 2.0;
                    MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
                    // Dispatch to the widget itself: going through screen.mouseClicked can hit an invisible
                    // listener lying on top of it (and then reports "handled" without pressing anything).
                    boolean handled = widget.mouseClicked(event, false);
                    widget.mouseReleased(event);
                    if (handled && widget.shouldTakeFocusAfterInteraction()) screen.setFocused(widget);
                    JsonObject out = new JsonObject();
                    out.addProperty("clicked", widget.getMessage().getString());
                    out.addProperty("handled", handled);
                    return out;
                }));

        bridge.register("client.setText", "index?:int | text?:string (current text or label), value:string",
                "Sets the text of a text field on the open screen.", p -> onClient(() -> {
                    AbstractWidget widget = findWidget(requireScreen(), p);
                    if (!(widget instanceof EditBox box)) throw new Params.InvalidParams("widget is not a text field");
                    box.setValue(p.string("value"));
                    return new JsonObject();
                }));

        bridge.register("client.screenshot", "", "Saves a screenshot PNG of the game window and returns its absolute path.", p -> {
            Minecraft mc = Minecraft.getInstance();
            Path dir = mc.gameDirectory.toPath().resolve("screenshots");
            Files.createDirectories(dir);
            Path file = dir.resolve("rose-bridge-" + System.currentTimeMillis() + ".png").toAbsolutePath();
            CompletableFuture<Path> done = new CompletableFuture<>();
            mc.execute(() -> Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
                try (image) {
                    image.writeToFile(file);
                    done.complete(file);
                } catch (Exception e) {
                    done.completeExceptionally(e);
                }
            }));
            JsonObject out = new JsonObject();
            out.addProperty("path", done.get(TIMEOUT_SECONDS, TimeUnit.SECONDS).toString());
            return out;
        });

        bridge.register("client.createTestWorld", "",
                "Creates and opens a new creative flat test world (no mobs, no time or weather changes), like vanilla's "
                        + "debug test world. Waits until the player is in it.", p -> {
                    Minecraft mc = Minecraft.getInstance();
                    onClient(() -> {
                        CreateWorldScreen.testWorld(mc, () -> mc.gui.setScreen(new TitleScreen()));
                        return null;
                    });
                    waitFor("the create-world screen", 30, () -> mc.gui.screen() instanceof CreateWorldScreen);
                    String create = Component.translatable("selectWorld.create").getString();
                    onClient(() -> {
                        Screen screen = requireScreen();
                        AbstractWidget button = widgets(screen).stream()
                                .filter(w -> w.getMessage().getString().equals(create)).findFirst()
                                .orElseThrow(() -> new IllegalStateException("no '" + create + "' button"));
                        button.onClick(new MouseButtonEvent(button.getX() + 1, button.getY() + 1, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
                        return null;
                    });
                    // Vanilla's test world uses a custom flat dimension setup, which vanilla itself labels
                    // experimental, so it asks for confirmation. That warning is expected here; confirm it.
                    String experimental = Component.translatable("selectWorld.warning.experimental.title").getString();
                    String yes = Component.translatable("gui.yes").getString();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(180);
                    while (System.nanoTime() < deadline) {
                        boolean inWorld = onClient(() -> {
                            if (mc.level != null && mc.player != null && mc.gui.screen() == null) return true;
                            if (mc.gui.screen() instanceof ConfirmScreen confirm && confirm.getTitle().getString().equals(experimental)) {
                                widgets(confirm).stream().filter(w -> w.getMessage().getString().equals(yes)).findFirst()
                                        .ifPresent(w -> w.onClick(new MouseButtonEvent(w.getX() + 1, w.getY() + 1,
                                                new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false));
                            }
                            return false;
                        });
                        if (inWorld) return onClient(ClientMethods::status);
                        Thread.sleep(250);
                    }
                    throw new IllegalStateException("timed out after 180s waiting for the test world to open");
                });

        bridge.register("client.openWorld", "name:string (the world's folder name in saves/)",
                "Opens an existing singleplayer world and waits until the player is in it.", p -> {
                    Minecraft mc = Minecraft.getInstance();
                    String name = p.string("name");
                    onClient(() -> {
                        mc.createWorldOpenFlows().openWorld(name, () -> mc.gui.setScreen(new TitleScreen()));
                        return null;
                    });
                    waitInWorld(mc, 180);
                    return onClient(ClientMethods::status);
                });

        bridge.register("client.connect", "address:string (host or host:port)",
                "Joins a multiplayer server and waits until the player is in the world.", p -> {
                    Minecraft mc = Minecraft.getInstance();
                    String address = p.string("address");
                    onClient(() -> {
                        ConnectScreen.startConnecting(new TitleScreen(), mc, ServerAddress.parseString(address),
                                new ServerData("Rose", address, ServerData.Type.OTHER), false, null);
                        return null;
                    });
                    waitInWorld(mc, 120);
                    return onClient(ClientMethods::status);
                });

        bridge.register("client.disconnect", "", "Leaves the current world or server (singleplayer worlds are saved) "
                + "and returns to the title screen.", p -> onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.disconnect(new TitleScreen(), false);
            return new JsonObject();
        }));

        bridge.register("client.player", "", "The local player: position, rotation, health, food, held item and inventory.",
                p -> onClient(() -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) throw new IllegalStateException("not in a world");
                    var player = mc.player;
                    JsonObject out = new JsonObject();
                    out.addProperty("name", player.getName().getString());
                    out.addProperty("x", player.getX());
                    out.addProperty("y", player.getY());
                    out.addProperty("z", player.getZ());
                    out.addProperty("yaw", player.getYRot());
                    out.addProperty("pitch", player.getXRot());
                    out.addProperty("health", player.getHealth());
                    out.addProperty("food", player.getFoodData().getFoodLevel());
                    out.addProperty("mainHand", stack(player.getMainHandItem()));
                    JsonArray inventory = new JsonArray();
                    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (item.isEmpty()) continue;
                        JsonObject entry = new JsonObject();
                        entry.addProperty("slot", slot);
                        entry.addProperty("item", stack(item));
                        inventory.add(entry);
                    }
                    out.add("inventory", inventory);
                    return out;
                }));

        bridge.register("client.useBlock", "x:int, y:int, z:int, face?:string (default up)",
                "The local player right-clicks a block with the main hand, exactly like a real click (goes through "
                        + "the client, the network and the server).", p -> onClient(() -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.gameMode == null) throw new IllegalStateException("not in a world");
                    Direction face = Direction.byName(p.string("face", "up").toLowerCase(Locale.ROOT));
                    if (face == null) throw new Params.InvalidParams("face must be one of down, up, north, south, west, east");
                    BlockPos pos = new BlockPos(p.integer("x"), p.integer("y"), p.integer("z"));
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
                    var result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                    JsonObject out = new JsonObject();
                    out.addProperty("result", result.toString());
                    return out;
                }));

        bridge.register("client.chat", "message:string (starting with / runs a command)",
                "Sends a chat message or command as the local player.", p -> onClient(() -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.getConnection() == null) throw new IllegalStateException("not connected");
                    String message = p.string("message");
                    if (message.startsWith("/")) mc.getConnection().sendCommand(message.substring(1));
                    else mc.getConnection().sendChat(message);
                    return new JsonObject();
                }));

        bridge.register("client.quit", "", "Closes the game client (saves and exits).", p -> onClient(() -> {
            Minecraft.getInstance().stop();
            return new JsonObject();
        }));
    }

    private static JsonObject status() {
        Minecraft mc = Minecraft.getInstance();
        JsonObject out = new JsonObject();
        Screen screen = mc.gui.screen();
        out.addProperty("screen", screen != null ? screen.getClass().getName() : null);
        out.addProperty("inWorld", mc.level != null && mc.player != null);
        out.addProperty("singleplayer", mc.hasSingleplayerServer());
        // Screens that animate (fades) only progress while frames are drawn; a minimized window draws none.
        out.addProperty("fps", mc.getFps());
        out.addProperty("windowMinimized", mc.getWindow().isIconified());
        out.addProperty("windowFocused", mc.isWindowActive());
        ServerData server = mc.getCurrentServer();
        out.addProperty("server", server != null ? server.ip : null);
        if (mc.player != null) {
            out.addProperty("x", mc.player.getX());
            out.addProperty("y", mc.player.getY());
            out.addProperty("z", mc.player.getZ());
        }
        return out;
    }

    private static String stack(ItemStack stack) {
        if (stack.isEmpty()) return "";
        return stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static JsonObject describe(int index, AbstractWidget widget) {
        JsonObject w = new JsonObject();
        w.addProperty("index", index);
        w.addProperty("type", widget.getClass().getSimpleName().isEmpty() ? widget.getClass().getName() : widget.getClass().getSimpleName());
        w.addProperty("text", widget instanceof EditBox box ? box.getValue() : widget.getMessage().getString());
        w.addProperty("active", widget.isActive());
        w.addProperty("visible", widget.visible);
        w.addProperty("x", widget.getX());
        w.addProperty("y", widget.getY());
        w.addProperty("width", widget.getWidth());
        w.addProperty("height", widget.getHeight());
        return w;
    }

    /** Every widget on the screen, depth-first, in a stable order. */
    private static List<AbstractWidget> widgets(ContainerEventHandler container) {
        List<AbstractWidget> out = new ArrayList<>();
        for (GuiEventListener child : container.children()) {
            if (child instanceof AbstractWidget widget) out.add(widget);
            if (child instanceof ContainerEventHandler nested) out.addAll(widgets(nested));
        }
        return out;
    }

    private static AbstractWidget findWidget(Screen screen, Params p) {
        List<AbstractWidget> all = widgets(screen);
        if (p.has("index")) {
            int index = p.integer("index");
            if (index < 0 || index >= all.size()) throw new Params.InvalidParams("no widget " + index + " (see client.screen)");
            return all.get(index);
        }
        String text = p.string("text");
        return all.stream()
                .filter(w -> w.visible && (w.getMessage().getString().equalsIgnoreCase(text)
                        || (w instanceof EditBox box && box.getValue().equalsIgnoreCase(text))))
                .findFirst()
                .orElseThrow(() -> new Params.InvalidParams("no visible widget with text '" + text + "' (see client.screen)"));
    }

    private static Screen requireScreen() {
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null) throw new IllegalStateException("no screen is open");
        return screen;
    }

    private static void waitInWorld(Minecraft mc, int seconds) throws Exception {
        waitFor("the player to be in the world", seconds,
                () -> mc.level != null && mc.player != null && mc.gui.screen() == null);
    }

    private static void waitFor(String what, int seconds, BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            if (onClient(condition::getAsBoolean)) return;
            Thread.sleep(250);
        }
        throw new IllegalStateException("timed out after " + seconds + "s waiting for " + what);
    }

    static <T> T onClient(Supplier<T> task) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread()) return task.get();
        return mc.submit(task).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private ClientMethods() {}
}
