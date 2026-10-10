package rose.era.v1_20_1.shim;

import net.minecraft.util.Mth;

/** Redirect targets for 1.20.1 {@code Mth} helpers that moved (colors went to ARGB with alpha) or changed signature. */
public final class MthShim {
    /** 1.20.1 {@code Mth.color(r, g, b)}: 0xRRGGBB without alpha. */
    public static int color(float r, float g, float b) {
        return color(Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
    }

    /** 1.20.1 {@code Mth.color(r, g, b)} for 0-255 ints: 0xRRGGBB without alpha. */
    public static int color(int r, int g, int b) {
        return (r << 8 | g) << 8 | b;
    }

    /** 1.20.1 {@code Mth.sin(float)}; it takes a double now. */
    public static float sin(float value) {
        return Mth.sin(value);
    }

    /** 1.20.1 {@code Mth.cos(float)}; it takes a double now. */
    public static float cos(float value) {
        return Mth.cos(value);
    }

    private MthShim() {}
}
