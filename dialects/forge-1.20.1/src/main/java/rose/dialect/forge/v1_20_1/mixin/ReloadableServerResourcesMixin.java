package rose.dialect.forge.v1_20_1.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.ForgeLootModifiers;

/** Forge's data reload also loaded global loot modifiers (LootModifierManager) and mods' listeners (AddReloadListenerEvent). */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void rose$forgeListeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        List<PreparableReloadListener> listeners = new ArrayList<>(cir.getReturnValue());
        listeners.add(new ForgeLootModifiers.Listener());
        if (ForgeDialect.active()) {
            AddReloadListenerEvent event = new AddReloadListenerEvent((ReloadableServerResources) (Object) this);
            MinecraftForge.EVENT_BUS.post(event);
            listeners.addAll(event.getListeners());
        }
        cir.setReturnValue(listeners);
    }
}
