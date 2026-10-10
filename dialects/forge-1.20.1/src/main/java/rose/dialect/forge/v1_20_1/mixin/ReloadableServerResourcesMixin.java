package rose.dialect.forge.v1_20_1.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeLootModifiers;

/** Forge's data reload also loaded global loot modifiers (LootModifierManager). */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void rose$forgeListeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        List<PreparableReloadListener> listeners = new ArrayList<>(cir.getReturnValue());
        listeners.add(new ForgeLootModifiers.Listener());
        cir.setReturnValue(listeners);
    }
}
