package rose.era.v1_20_1.shim;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Redirect targets for 1.20.1 {@code ItemStack} methods that changed in 26.3. */
public final class ItemStackShim {
    /** The entity holding the stack whose 1.20.1 item property is being read (LegacyItemProperty), if any. */
    private static final ThreadLocal<net.minecraft.world.entity.Entity> PROPERTY_OWNER = new ThreadLocal<>();

    /** Runs {@code read} with {@code owner} as the holder 1.20.1 {@code isFramed}/{@code getFrame} answer from. */
    public static float withPropertyOwner(net.minecraft.world.entity.Entity owner, java.util.function.Supplier<Float> read) {
        net.minecraft.world.entity.Entity previous = PROPERTY_OWNER.get();
        PROPERTY_OWNER.set(owner);
        try {
            return read.get();
        } finally {
            PROPERTY_OWNER.set(previous);
        }
    }

    /**
     * 1.20.1 {@code stack.isFramed()}: stacks remembered the item frame showing them; 26.3 stacks don't, and item
     * models get the frame as their owner instead, which is known while a 1.20.1 item property is read.
     */
    public static boolean isFramed(ItemStack self) {
        return PROPERTY_OWNER.get() instanceof net.minecraft.world.entity.decoration.ItemFrame;
    }

    /** 1.20.1 {@code stack.getFrame()}: see {@link #isFramed}. */
    public static net.minecraft.world.entity.decoration.ItemFrame getFrame(ItemStack self) {
        return PROPERTY_OWNER.get() instanceof net.minecraft.world.entity.decoration.ItemFrame frame ? frame : null;
    }

    /**
     * 1.20.1 {@code ItemStack.is(Item)}: {@code return this.getItem() == item;}. 26.3 only has
     * {@code is(Predicate<Holder<Item>>)}, but {@code getItem()} is unchanged.
     */
    public static boolean is(ItemStack self, Item item) {
        return self.getItem() == item;
    }

    /** 1.20.1 {@code onCraftedBy(level, player, amount)}; 26.3 dropped the level (same stat and item callback). */
    public static void onCraftedBy(ItemStack self, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, int amount) {
        self.onCraftedBy(player, amount);
    }

    private ItemStackShim() {}

    /**
     * 1.20.1 {@code getAttributeModifiers(slot)}: the stack's modifiers for a slot by attribute. 1.20.5 moved them to
     * the attribute_modifiers component (26.3 forEachModifier), keyed by attribute holders.
     */
    public static com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier>
            getAttributeModifiers(ItemStack self, net.minecraft.world.entity.EquipmentSlot slot) {
        com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> out =
                com.google.common.collect.LinkedHashMultimap.create();
        self.forEachModifier(slot, (attribute, modifier) -> out.put(attribute.value(), modifier));
        return out;
    }
}
