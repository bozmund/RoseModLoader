package net.minecraft.world.entity.npc;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Era bridge (1.20.1): {@code VillagerTrades.ItemListing}, one possible trade. 26.x trades are data-driven
 * (VillagerTrade in trade sets); dialects turn old listings into offers by calling {@link #getOffer}.
 * (A top-level type named like the old nested one: the old class is gone, so old code links to this.)
 */
public interface VillagerTrades$ItemListing {
    MerchantOffer getOffer(Entity trader, RandomSource random);
}
