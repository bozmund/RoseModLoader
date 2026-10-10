package net.minecraft.client.renderer.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Era bridge (1.20.1): a number an item model's {@code overrides} pick a model by. 26.x replaced it with item model
 * definitions; packfix turns old overrides into a definition that asks these (LegacyItemProperty).
 */
@FunctionalInterface
public interface ItemPropertyFunction {
    float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
}
