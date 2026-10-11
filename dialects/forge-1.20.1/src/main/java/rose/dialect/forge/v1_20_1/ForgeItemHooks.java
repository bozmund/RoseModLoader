package rose.dialect.forge.v1_20_1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Forge 1.20.1 {@code IForgeItem} hooks that 26.3's Item doesn't have. A mod item that overrides one declares it as a
 * plain method (Rose's Item has no such method to override), so it is found by name, once per item class.
 */
public final class ForgeItemHooks {
    private static final Map<Class<?>, Optional<Method>> USE_FIRST = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<Method>> INIT_CAPABILITIES = new ConcurrentHashMap<>();
    private static final net.minecraft.resources.Identifier ITEM_CAPABILITIES = net.minecraft.resources.Identifier.fromNamespaceAndPath("forge", "item");

    /**
     * {@code IForgeItem.initCapabilities(stack, nbt)}: the provider an item attaches to each of its stacks, keyed like
     * Forge's own item capability, or none.
     */
    public static Map<net.minecraft.resources.Identifier, net.minecraftforge.common.capabilities.ICapabilityProvider> initCapabilities(ItemStack stack) {
        Item item = stack.getItem();
        Method hook = INIT_CAPABILITIES.computeIfAbsent(item.getClass(), type -> {
            try {
                return Optional.of(type.getMethod("initCapabilities", ItemStack.class, net.minecraft.nbt.CompoundTag.class));
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }).orElse(null);
        if (hook == null) return Map.of();
        try {
            Object provider = hook.invoke(item, stack, null);
            return provider instanceof net.minecraftforge.common.capabilities.ICapabilityProvider p ? Map.of(ITEM_CAPABILITIES, p) : Map.of();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(item + ".initCapabilities failed", e instanceof InvocationTargetException ite ? ite.getCause() : e);
        }
    }

    /**
     * {@code IForgeItem.onItemUseFirst(stack, context)}: runs before the block is used (Forge's ServerPlayerGameMode
     * and MultiPlayerGameMode patches); anything but PASS ends the interaction with that result.
     */
    public static InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Item item = stack.getItem();
        Method hook = USE_FIRST.computeIfAbsent(item.getClass(), ForgeItemHooks::useFirstMethod).orElse(null);
        if (hook == null) return InteractionResult.PASS;
        try {
            return (InteractionResult) hook.invoke(item, stack, context);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException r) throw r;
            if (e.getCause() instanceof Error err) throw err;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Optional<Method> useFirstMethod(Class<?> type) {
        try {
            Method m = type.getMethod("onItemUseFirst", ItemStack.class, UseOnContext.class);
            return m.getReturnType() == InteractionResult.class ? Optional.of(m) : Optional.empty();
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private ForgeItemHooks() {}
}
