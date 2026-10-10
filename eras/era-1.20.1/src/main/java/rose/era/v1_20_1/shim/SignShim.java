package rose.era.v1_20_1.shim;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

/** Redirect targets for 1.20.1 sign block entity members. */
public final class SignShim {
    /** 1.20.1 {@code SignBlockEntity.createDefaultSignText()} (protected, gone in 26.x): an empty sign text (26.3 SignText.EMPTY, as SignBlockEntity starts with). */
    public static SignText createDefaultSignText(SignBlockEntity self) {
        return SignText.EMPTY;
    }

    /** 1.20.1 {@code SignText.setColor(color)}; 26.3 calls it {@code withColor} (SignText stayed immutable). */
    public static SignText setColor(SignText self, net.minecraft.world.item.DyeColor color) {
        return self.withColor(color);
    }

    /** 1.20.1 {@code SignText.setHasGlowingText(glowing)}; 26.3 {@code withGlowingText}. */
    public static SignText setHasGlowingText(SignText self, boolean glowing) {
        return self.withGlowingText(glowing);
    }

    private SignShim() {}
}
