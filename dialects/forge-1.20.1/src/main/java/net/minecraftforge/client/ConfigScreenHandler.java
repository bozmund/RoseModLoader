package net.minecraftforge.client;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

/**
 * Mods' config screens (Forge 1.20.1): Forge's mod list opened them. Rose has no mod list screen yet, so the
 * factories are kept and can be looked up, but nothing in the game opens them.
 */
public class ConfigScreenHandler {
    public record ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) implements IExtensionPoint<ConfigScreenFactory> {
        public ConfigScreenFactory(Function<Screen, Screen> screenFunction) {
            this((minecraft, screen) -> screenFunction.apply(screen));
        }
    }

    public static Optional<BiFunction<Minecraft, Screen, Screen>> getScreenFactoryFor(IModInfo mod) {
        return ModList.get().getModContainerById(mod.getModId())
                .flatMap(container -> container.getCustomExtension(ConfigScreenFactory.class))
                .map(ConfigScreenFactory::screenFunction);
    }
}
