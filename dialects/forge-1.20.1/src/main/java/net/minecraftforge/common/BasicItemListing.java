package net.minecraftforge.common;

import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * A simple villager trade. 26.3 villager trades are data-driven (the {@code villager_trade} registry), so Rose
 * collects these from VillagerTradesEvent listeners and offers them with the trade sets' own (ForgeTrades).
 */
public class BasicItemListing {
    protected final ItemStack price;
    protected final ItemStack price2;
    protected final ItemStack forSale;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public BasicItemListing(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this.price = price;
        this.price2 = price2;
        this.forSale = forSale;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public BasicItemListing(ItemStack price, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(price, ItemStack.EMPTY, forSale, maxTrades, xp, priceMult);
    }

    public BasicItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp, float mult) {
        this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, mult);
    }

    public BasicItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp) {
        this(emeralds, forSale, maxTrades, xp, 1);
    }

    /** Forge 1.20.1 {@code getOffer}: always the same offer (26.3 costs match the item and count). */
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        Optional<ItemCost> second = price2.isEmpty() ? Optional.empty() : Optional.of(cost(price2));
        return new MerchantOffer(cost(price), second, forSale.copy(), maxTrades, xp, priceMult);
    }

    private static ItemCost cost(ItemStack stack) {
        return new ItemCost(stack.getItem(), stack.getCount());
    }
}
