package rose.dialect.forge.v1_20_1.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Client tooltip renderers for mod tooltip components (consulted by a ClientTooltipComponent hook). */
public final class TooltipFactories {
    private static final Map<Class<?>, Function<?, ? extends ClientTooltipComponent>> FACTORIES = new ConcurrentHashMap<>();

    public static <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory) {
        FACTORIES.put(type, factory);
    }

    @SuppressWarnings("unchecked")
    public static ClientTooltipComponent create(TooltipComponent component) {
        Function<Object, ? extends ClientTooltipComponent> factory = (Function<Object, ? extends ClientTooltipComponent>) FACTORIES.get(component.getClass());
        return factory != null ? factory.apply(component) : null;
    }

    private TooltipFactories() {}
}
