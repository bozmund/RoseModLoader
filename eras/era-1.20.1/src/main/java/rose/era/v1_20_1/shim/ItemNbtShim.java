package rose.era.v1_20_1.shim;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import rose.era.v1_20_1.EraContext;

/**
 * Redirect targets for 1.20.1 item NBT. 1.20.5 replaced the stack's tag with data components; free-form mod data
 * lives in {@code minecraft:custom_data}. 1.20.1 handed out the live tag (mods edit it in place), so the shim stores
 * the very tag it returns in the component instead of a copy.
 */
public final class ItemNbtShim {
    /** 1.20.1 {@code stack.getTag()}: the live tag, or {@code null}. */
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : live(stack, data.copyTag());
    }

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return live(stack, data == null ? new CompoundTag() : data.copyTag());
    }

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (tag == null) stack.remove(DataComponents.CUSTOM_DATA);
        else live(stack, tag);
    }

    /** 1.20.1 {@code getTagElement(key)}: the live sub-tag, or {@code null}. */
    public static CompoundTag getTagElement(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.get(key) instanceof CompoundTag element ? element : null;
    }

    public static void addTagElement(ItemStack stack, String key, Tag element) {
        getOrCreateTag(stack).put(key, element);
    }

    /** 1.20.1 kept the custom name under display.Name; 26.3 has the {@code custom_name} component. */
    public static boolean hasCustomHoverName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME);
    }

    public static ItemStack setHoverName(ItemStack stack, Component name) {
        if (name == null) stack.remove(DataComponents.CUSTOM_NAME);
        else stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }

    /** 1.20.1 {@code isEdible()}: the item had food properties; 26.3 has the {@code food} component. */
    public static boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    /** 1.20.1 {@code stack.save(tag)}; written in 26.3's item format, which {@link #of} reads back. */
    public static CompoundTag save(ItemStack stack, CompoundTag into) {
        if (stack.isEmpty()) return into;
        Tag encoded = ItemStack.CODEC.encodeStart(ops(), stack).getOrThrow();
        if (encoded instanceof CompoundTag c) into.merge(c);
        return into;
    }

    /** Forge {@code IForgeItemStack.serializeNBT()}. */
    public static CompoundTag serializeNBT(ItemStack stack) {
        return save(stack, new CompoundTag());
    }

    /** 1.20.1 {@code ItemStack.of(tag)}: reads 26.3's format and 1.20.1's ({@code id}, {@code Count}, {@code tag}). */
    public static ItemStack of(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return ItemStack.EMPTY;
        if (tag.contains("Count") && !tag.contains("count")) {
            Identifier id = Identifier.tryParse(tag.getStringOr("id", ""));
            Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
            if (item == null) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(item, tag.getByteOr("Count", (byte) 0));
            if (tag.get("tag") instanceof CompoundTag old) live(stack, old.copy());
            return stack;
        }
        return ItemStack.OPTIONAL_CODEC.parse(ops(), tag).result().orElse(ItemStack.EMPTY);
    }

    private static CompoundTag live(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, new CustomData(tag));
        return tag;
    }

    private static RegistryOps<Tag> ops() {
        return RegistryOps.create(NbtOps.INSTANCE, EraContext.registryAccess());
    }

    private ItemNbtShim() {}
}
