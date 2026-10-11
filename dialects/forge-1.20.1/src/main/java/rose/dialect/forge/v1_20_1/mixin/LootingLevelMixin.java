package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/**
 * Forge 1.20.1 LootingLevelEvent (ForgeHooks.getLootingLevel): the looting level a killed entity's loot uses. 26.x
 * applies looting in enchanted_count_increase functions that name the looting enchantment.
 */
@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class LootingLevelMixin {
    @Shadow
    @Final
    private Holder<Enchantment> enchantment;

    @ModifyVariable(method = "run", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int rose$lootingLevel(int level, ItemStack stack, LootContext context) {
        if (!ForgeDialect.active() || !enchantment.is(Enchantments.LOOTING)
                || !((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(LootingLevelEvent.class)) return level;
        if (!(context.getOptional(LootContextParams.THIS_ENTITY) instanceof LivingEntity target)) return level;
        LootingLevelEvent event = new LootingLevelEvent(target, context.getOptional(LootContextParams.DAMAGE_SOURCE), level);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getLootingLevel();
    }
}
