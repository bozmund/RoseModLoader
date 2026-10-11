package rose.era.v1_20_1.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import rose.era.v1_20_1.client.InputBridges;

/**
 * Screen's 1.20.1-shaped input methods, for old screens' {@code super.mouseClicked(x, y, button)} and the like: they
 * call the 26.x method as a marked super call (InputBridges), which runs the game's implementation.
 */
@Mixin(Screen.class)
public abstract class ScreenInputMixin {
    public boolean mouseClicked(double x, double y, int button) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)), false);
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean mouseReleased(double x, double y, int button) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.mouseReleased(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)));
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.mouseDragged(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)), dx, dy);
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean mouseScrolled(double x, double y, double delta) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.mouseScrolled(x, y, 0, delta);
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.keyPressed(new KeyEvent(key, scancode, modifiers));
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean keyReleased(int key, int scancode, int modifiers) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.keyReleased(new KeyEvent(key, scancode, modifiers));
        } finally {
            InputBridges.endSuper(self);
        }
    }

    public boolean charTyped(char c, int modifiers) {
        Screen self = (Screen) (Object) this;
        InputBridges.beginSuper(self);
        try {
            return self.charTyped(new CharacterEvent(c));
        } finally {
            InputBridges.endSuper(self);
        }
    }
}
