package net.minecraftforge.common.loot;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/** Changes the drops of every loot table it applies to (data: {@code data/forge/loot_modifiers/}). */
public interface IGlobalLootModifier {
    ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);

    Codec<? extends IGlobalLootModifier> codec();
}
