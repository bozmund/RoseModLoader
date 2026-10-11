package rose.era.v1_20_1.shim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

/**
 * Redirect targets for 1.20.1 {@code CompoundTag} getters. 1.21.5 made them return {@code Optional}; the old ones
 * returned a default (0, false, "", an empty tag or array) when the key was missing or had another type.
 */
public final class NbtShim {
    public static CompoundTag getCompound(CompoundTag self, String key) {
        return self.getCompoundOrEmpty(key);
    }

    public static int getInt(CompoundTag self, String key) {
        return self.getIntOr(key, 0);
    }

    public static long getLong(CompoundTag self, String key) {
        return self.getLongOr(key, 0L);
    }

    public static float getFloat(CompoundTag self, String key) {
        return self.getFloatOr(key, 0f);
    }

    public static double getDouble(CompoundTag self, String key) {
        return self.getDoubleOr(key, 0d);
    }

    public static byte getByte(CompoundTag self, String key) {
        return self.getByteOr(key, (byte) 0);
    }

    public static short getShort(CompoundTag self, String key) {
        return self.getShortOr(key, (short) 0);
    }

    public static boolean getBoolean(CompoundTag self, String key) {
        return self.getBooleanOr(key, false);
    }

    public static String getString(CompoundTag self, String key) {
        return self.getStringOr(key, "");
    }

    public static int[] getIntArray(CompoundTag self, String key) {
        return self.getIntArray(key).orElseGet(() -> new int[0]);
    }

    /** 1.20.1 {@code contains(key, type)}: type 99 meant "any number". */
    public static boolean contains(CompoundTag self, String key, int type) {
        Tag tag = self.get(key);
        if (tag == null) return false;
        if (tag.getId() == type) return true;
        return type == 99 && tag instanceof NumericTag;
    }

    private NbtShim() {}

    /**
     * 1.20.1 {@code getList(key, elementType)}: the list under {@code key} if its elements have that tag type, else an
     * empty list. 1.21.5 getters return Optionals and lists may mix types.
     */
    public static net.minecraft.nbt.ListTag getList(CompoundTag self, String key, int elementType) {
        net.minecraft.nbt.ListTag list = self.getListOrEmpty(key);
        if (list.isEmpty()) return list;
        for (Tag element : list) {
            if (element.getId() != elementType) return new net.minecraft.nbt.ListTag();
        }
        return list;
    }

    /** 1.20.1 {@code ListTag.getCompound(index)}: an empty compound when absent. */
    public static CompoundTag getCompound(net.minecraft.nbt.ListTag self, int index) {
        return self.getCompoundOrEmpty(index);
    }

    /** 1.20.1 {@code ListTag.getInt(index)}: 0 when absent. */
    public static int getInt(net.minecraft.nbt.ListTag self, int index) {
        return self.getIntOr(index, 0);
    }

    /** 1.20.1 {@code ListTag.getString(index)}: "" when absent. */
    public static String getString(net.minecraft.nbt.ListTag self, int index) {
        return self.getStringOr(index, "");
    }

    /** 1.20.1 {@code getUUID(key)}: stored as four ints; 1.21.5 reads it with UUIDUtil.CODEC. */
    public static java.util.UUID getUUID(CompoundTag self, String key) {
        return self.read(key, net.minecraft.core.UUIDUtil.CODEC).orElseThrow(() -> new IllegalArgumentException("No UUID under " + key));
    }

    /** 1.20.1 {@code putUUID(key, uuid)}. */
    public static void putUUID(CompoundTag self, String key, java.util.UUID uuid) {
        self.store(key, net.minecraft.core.UUIDUtil.CODEC, uuid);
    }

    /** 1.20.1 {@code StringTag.getAsString()}: the string itself; 26.3 StringTag is a record of its value. */
    public static String getAsString(net.minecraft.nbt.StringTag self) {
        return self.value();
    }

    /** 1.20.1 {@code TagParser.parseTag(text)}; 26.3 {@code parseCompoundFully}. */
    public static CompoundTag parseTag(String text) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return net.minecraft.nbt.TagParser.parseCompoundFully(text);
    }

    /** 1.20.1 {@code NbtIo.readCompressed(stream)} read without a size limit; 26.3 takes an accounter. */
    public static CompoundTag readCompressed(java.io.InputStream in) throws java.io.IOException {
        return net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.create(Long.MAX_VALUE));
    }
}
