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
}
