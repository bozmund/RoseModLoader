package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.client.CreativeTabPages;

/** A paged mod tab's place on its page while the creative screen lays out tabs (see CreativeTabPages). */
@Mixin(CreativeModeTab.class)
public abstract class CreativeTabPositionMixin implements CreativeTabPages.PagedTab {
    @Shadow
    @Final
    private int column;

    @Override
    public int rose$storedColumn() {
        return column;
    }

    @Inject(method = "column", at = @At("HEAD"), cancellable = true)
    private void rose$pageColumn(CallbackInfoReturnable<Integer> cir) {
        if (column < CreativeTabPages.FIRST_MOD_COLUMN) return;
        int[] position = CreativeTabPages.displayPosition((CreativeModeTab) (Object) this);
        if (position != null) cir.setReturnValue(position[1]);
    }

    @Inject(method = "row", at = @At("HEAD"), cancellable = true)
    private void rose$pageRow(CallbackInfoReturnable<CreativeModeTab.Row> cir) {
        if (column < CreativeTabPages.FIRST_MOD_COLUMN) return;
        int[] position = CreativeTabPages.displayPosition((CreativeModeTab) (Object) this);
        if (position != null) cir.setReturnValue(position[0] == 0 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM);
    }
}
