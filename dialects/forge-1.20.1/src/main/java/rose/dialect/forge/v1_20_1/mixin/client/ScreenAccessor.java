package rose.dialect.forge.v1_20_1.mixin.client;

import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** ScreenEvent.Init adds and removes widgets the way the screen itself does. */
@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("addRenderableWidget")
    GuiEventListener rose$addRenderableWidget(GuiEventListener widget);

    @Invoker("addWidget")
    GuiEventListener rose$addWidget(GuiEventListener widget);

    @Invoker("removeWidget")
    void rose$removeWidget(GuiEventListener widget);

    @Accessor("children")
    List<GuiEventListener> rose$children();
}
