package rose.era.v1_20_1.client;

import java.lang.invoke.MethodType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import rose.era.v1_20_1.bridge.Legacy;

/**
 * Input for 1.20.1 screens (rules/forge-1.20.1/bridges.tsv). 26.x passes input as events (MouseButtonEvent,
 * KeyEvent, CharacterEvent); 1.20.1 screens override {@code mouseClicked(x, y, button)}, {@code keyPressed(key,
 * scancode, modifiers)} and so on, and call {@code super} with those. The bridges call the old overrides; the old
 * {@code super} calls reach Screen's old-shaped methods (ScreenInputMixin), which come back here marked as super
 * calls and run the game's own implementation.
 */
public final class InputBridges {
    private static final ThreadLocal<Object> SUPER_CALL = new ThreadLocal<>();

    /** Marks the next bridged event on {@code self} as a mod's {@code super} call. */
    public static void beginSuper(Object self) {
        SUPER_CALL.set(self);
    }

    public static void endSuper(Object self) {
        if (SUPER_CALL.get() == self) SUPER_CALL.remove();
    }

    private static boolean isSuperCall(Object self) {
        if (SUPER_CALL.get() != self) return false;
        SUPER_CALL.remove();
        return true;
    }

    public static boolean mouseClicked(Screen self, MouseButtonEvent event, boolean doubleClick) {
        if (isSuperCall(self)) return call(self, "mouseClicked", MethodType.methodType(boolean.class, MouseButtonEvent.class, boolean.class), event, doubleClick);
        return old(self, "mouseClicked", MethodType.methodType(boolean.class, double.class, double.class, int.class), event.x(), event.y(), event.button());
    }

    public static boolean mouseReleased(Screen self, MouseButtonEvent event) {
        if (isSuperCall(self)) return call(self, "mouseReleased", MethodType.methodType(boolean.class, MouseButtonEvent.class), event);
        return old(self, "mouseReleased", MethodType.methodType(boolean.class, double.class, double.class, int.class), event.x(), event.y(), event.button());
    }

    public static boolean mouseDragged(Screen self, MouseButtonEvent event, double dx, double dy) {
        if (isSuperCall(self)) return call(self, "mouseDragged", MethodType.methodType(boolean.class, MouseButtonEvent.class, double.class, double.class), event, dx, dy);
        return old(self, "mouseDragged", MethodType.methodType(boolean.class, double.class, double.class, int.class, double.class, double.class),
                event.x(), event.y(), event.button(), dx, dy);
    }

    /** 1.20.1 scrolled vertically only ({@code mouseScrolled(x, y, delta)}). */
    public static boolean mouseScrolled(Screen self, double x, double y, double scrollX, double scrollY) {
        if (isSuperCall(self)) return call(self, "mouseScrolled", MethodType.methodType(boolean.class, double.class, double.class, double.class, double.class), x, y, scrollX, scrollY);
        return old(self, "mouseScrolled", MethodType.methodType(boolean.class, double.class, double.class, double.class), x, y, scrollY);
    }

    public static boolean keyPressed(Screen self, KeyEvent event) {
        if (isSuperCall(self)) return call(self, "keyPressed", MethodType.methodType(boolean.class, KeyEvent.class), event);
        return old(self, "keyPressed", MethodType.methodType(boolean.class, int.class, int.class, int.class), event.key(), event.keycode(), event.modifiers());
    }

    public static boolean keyReleased(Screen self, KeyEvent event) {
        if (isSuperCall(self)) return call(self, "keyReleased", MethodType.methodType(boolean.class, KeyEvent.class), event);
        return old(self, "keyReleased", MethodType.methodType(boolean.class, int.class, int.class, int.class), event.key(), event.keycode(), event.modifiers());
    }

    /** 1.20.1 passed a char and the modifiers; 26.x a code point. */
    public static boolean charTyped(Screen self, CharacterEvent event) {
        if (isSuperCall(self)) return call(self, "charTyped", MethodType.methodType(boolean.class, CharacterEvent.class), event);
        if (!Character.isBmpCodePoint(event.codepoint())) return false;
        return old(self, "charTyped", MethodType.methodType(boolean.class, char.class, int.class), (char) event.codepoint(), 0);
    }

    /** 1.20.1 {@code hasClickedOutside(x, y, left, top, button)}; the button isn't passed any more. */
    public static boolean hasClickedOutside(AbstractContainerScreen<?> self, double x, double y, int left, int top) {
        return old(self, "hasClickedOutside", MethodType.methodType(boolean.class, double.class, double.class, int.class, int.class, int.class), x, y, left, top, 0);
    }

    private static boolean old(Object self, String name, MethodType type, Object... args) {
        Object[] all = new Object[args.length + 1];
        all[0] = self;
        System.arraycopy(args, 0, all, 1, args.length);
        return (Boolean) Legacy.invoke(Legacy.require(self, name, type), all);
    }

    private static boolean call(Object self, String name, MethodType type, Object... args) {
        Object[] all = new Object[args.length + 1];
        all[0] = self;
        System.arraycopy(args, 0, all, 1, args.length);
        return (Boolean) Legacy.invoke(Legacy.superMethod(self, name, type), all);
    }

    private InputBridges() {}
}
