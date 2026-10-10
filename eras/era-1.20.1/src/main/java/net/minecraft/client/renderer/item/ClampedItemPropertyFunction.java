package net.minecraft.client.renderer.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Era bridge (1.20.1): an item property kept between 0 and 1. */
@FunctionalInterface
public interface ClampedItemPropertyFunction extends ItemPropertyFunction {
    @Override
    default float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return Mth.clamp(unclampedCall(stack, level, entity, seed), 0.0F, 1.0F);
    }

    float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
}
