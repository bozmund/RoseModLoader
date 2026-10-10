package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeTrades;

/** Villagers and wandering traders draw from Forge mods' trades too (ForgeTrades). */
@Mixin(AbstractVillager.class)
public abstract class ForgeTradesMixin {
    @Inject(method = "addOffersFromTradeSet", at = @At("HEAD"), cancellable = true)
    private void rose$forgeTrades(ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> key, CallbackInfo ci) {
        if (ForgeTrades.addOffers((AbstractVillager) (Object) this, level, offers, key)) ci.cancel();
    }
}
