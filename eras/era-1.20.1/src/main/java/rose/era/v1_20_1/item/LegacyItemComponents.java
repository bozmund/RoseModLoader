package rose.era.v1_20_1.item;

import com.google.common.collect.Multimap;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import rose.era.v1_20_1.shim.Holders;

/**
 * 1.20.1 items described some properties by overriding methods; 26.3 reads item components instead. An item that
 * overrides one gets the component it implies:
 * <ul>
 *   <li>{@code getUseAnimation} = DRINK: a drink {@code consumable} (drink animation and sound, no particles), as
 *       1.20.1's LivingEntity played the drinking sound for that animation;</li>
 *   <li>{@code getDefaultAttributeModifiers(slot)}: {@code attribute_modifiers};</li>
 *   <li>{@code getEnchantmentValue()}: {@code enchantable}.</li>
 * </ul>
 * Items are asked once, with an empty stack, while components are built: components are fixed per item, and item
 * stacks of the item can't exist before then.
 */
public final class LegacyItemComponents {
    /** Call after the item is registered. */
    public static void register(Item item) {
        ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
        if (key == null) return;
        Method attributes = legacyMethod(item, "getDefaultAttributeModifiers", EquipmentSlot.class);
        Method enchantmentValue = legacyMethod(item, "getEnchantmentValue");
        boolean useAnimation = overrides(item, "getUseAnimation", ItemStack.class);
        if (attributes == null && enchantmentValue == null && !useAnimation) return;
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(key, (components, context, k) -> {
            if (useAnimation && ask(() -> item.getUseAnimation(ItemStack.EMPTY)) == ItemUseAnimation.DRINK) {
                components.set(DataComponents.CONSUMABLE, drink(item, (Consumable) map(components).get(DataComponents.CONSUMABLE)));
            }
            if (attributes != null) {
                ItemAttributeModifiers modifiers = attributeModifiers(item, attributes);
                if (!modifiers.modifiers().isEmpty()) components.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
            }
            if (enchantmentValue != null) {
                Integer value = ask(() -> (Integer) enchantmentValue.invoke(item));
                if (value != null && value > 0) components.set(DataComponents.ENCHANTABLE, new Enchantable(value));
            }
        });
    }

    private static final ClassValue<Method> REPAIR = new ClassValue<>() {
        @Override
        protected Method computeValue(Class<?> type) {
            try {
                return type.getMethod("isValidRepairItem", ItemStack.class, ItemStack.class);
            } catch (NoSuchMethodException e) {
                return null; // 26.3 Item has no such method: only translated items do
            }
        }
    };

    /**
     * 1.20.1 {@code Item.isValidRepairItem(stack, repair)} for an item that overrides it (ItemStackRepairMixin), else
     * {@code null}: the {@code repairable} component decides.
     */
    public static Boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        Method legacy = REPAIR.get(stack.getItem().getClass());
        if (legacy == null) return null;
        try {
            return (Boolean) legacy.invoke(stack.getItem(), stack, repair);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("isValidRepairItem of " + stack.getItem().getClass().getName() + " failed", e);
        }
    }

    /** The item's consumable as a drink; keeps what eating it does (food effects) and how long it takes. */
    private static Consumable drink(Item item, Consumable existing) {
        Consumable.Builder drink = Consumable.builder()
                .animation(ItemUseAnimation.DRINK)
                .sound(SoundEvents.GENERIC_DRINK)
                .hasConsumeParticles(false);
        if (existing != null) {
            drink.consumeSeconds(existing.consumeSeconds());
            existing.onConsumeEffects().forEach(drink::onConsume);
        } else {
            Integer ticks = ask(() -> item.getUseDuration(ItemStack.EMPTY, null));
            if (ticks != null && ticks > 0) drink.consumeSeconds(ticks / 20.0F);
        }
        return drink.build();
    }

    @SuppressWarnings("unchecked")
    private static ItemAttributeModifiers attributeModifiers(Item item, Method getDefaultAttributeModifiers) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> modifiers = ask(() -> (Multimap<Attribute, AttributeModifier>) getDefaultAttributeModifiers.invoke(item, slot));
            if (modifiers == null) continue;
            modifiers.forEach((attribute, modifier) -> builder.add(Holders.attribute(attribute), modifier, EquipmentSlotGroup.bySlot(slot)));
        }
        return builder.build();
    }

    /** A public method the 26.3 Item doesn't have (1.20.1 API kept by translation), or {@code null}. */
    private static Method legacyMethod(Item item, String name, Class<?>... parameters) {
        try {
            return item.getClass().getMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean overrides(Item item, String name, Class<?>... parameters) {
        Method method = legacyMethod(item, name, parameters);
        return method != null && method.getDeclaringClass() != Item.class;
    }

    private static Map<?, ?> map(DataComponentMap.Builder components) {
        return components.map;
    }

    @FunctionalInterface
    private interface Question<T> {
        T ask() throws ReflectiveOperationException;
    }

    /** The item's answer, or {@code null} if old code can't answer for an empty stack. */
    private static <T> T ask(Question<T> question) {
        try {
            return question.ask();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private LegacyItemComponents() {}
}
