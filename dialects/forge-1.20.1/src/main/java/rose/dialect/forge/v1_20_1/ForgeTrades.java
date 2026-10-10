package rose.dialect.forge.v1_20_1;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;

/**
 * Forge 1.20.1's villager trade events. Forge fired them when a server was about to start (VillagerTradingManager):
 * each profession's trade lists per level (1-5), and the wandering trader's generic and rare lists. A villager then
 * picked a level's offers at random from vanilla's and the mods' trades together. 26.3 trades are data-driven trade
 * sets, so the mods' listings are kept per trade set and drawn from with its trades (ForgeTradesMixin).
 */
public final class ForgeTrades {
    private static volatile Map<ResourceKey<TradeSet>, List<Object>> byTradeSet = Map.of();

    /** Asks the mods for their trades; call when a server is about to start. */
    static void load() {
        Map<ResourceKey<TradeSet>, List<Object>> out = new HashMap<>();
        for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
            Int2ObjectMap<List<Object>> trades = new Int2ObjectOpenHashMap<>();
            for (int level = 1; level <= 5; level++) trades.put(level, new ArrayList<>());
            MinecraftForge.EVENT_BUS.post(new VillagerTradesEvent(trades, profession));
            for (int level = 1; level <= 5; level++) add(out, profession.getTrades(level), trades.get(level));
        }
        List<Object> generic = new ArrayList<>();
        List<Object> rare = new ArrayList<>();
        MinecraftForge.EVENT_BUS.post(new WandererTradesEvent(generic, rare));
        add(out, TradeSets.WANDERING_TRADER_COMMON, generic);
        add(out, TradeSets.WANDERING_TRADER_UNCOMMON, rare);
        byTradeSet = Map.copyOf(out);
    }

    private static void add(Map<ResourceKey<TradeSet>, List<Object>> out, ResourceKey<TradeSet> set, List<Object> listings) {
        if (set != null && !listings.isEmpty()) out.computeIfAbsent(set, k -> new ArrayList<>()).addAll(listings);
    }

    /** The mods' 1.20.1 trade listings for a trade set. */
    public static List<Object> listings(ResourceKey<TradeSet> set) {
        return byTradeSet.getOrDefault(set, List.of());
    }

    /**
     * 26.3 {@code AbstractVillager.addOffersFromTradeSet} with the mods' listings in the pool: as many offers as the
     * set gives, drawn from its trades and the listings alike. Returns {@code false} (vanilla goes on) when no mod
     * added to this set.
     */
    public static boolean addOffers(AbstractVillager trader, ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> key) {
        List<Object> listings = listings(key);
        if (listings.isEmpty()) return false;
        Optional<TradeSet> found = trader.registryAccess().lookupOrThrow(Registries.TRADE_SET).getOptional(key);
        if (found.isEmpty()) return false;
        TradeSet set = found.get();
        LootContext context = new LootContext.Builder(new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, trader.position())
                .withParameter(LootContextParams.THIS_ENTITY, trader)
                .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
                .create(LootContextParamSets.VILLAGER_TRADE))
                .create(set.randomSequence());
        List<Function<LootContext, MerchantOffer>> pool = new ArrayList<>();
        for (Holder<VillagerTrade> trade : set.trades()) pool.add(c -> trade.value().getOffer(c));
        for (Object listing : listings) pool.add(c -> offer(listing, trader, c.getRandom()));
        int wanted = set.calculateNumberOfTrades(context);
        int added = 0;
        while (added < wanted && !pool.isEmpty()) {
            int roll = context.getRandom().nextInt(pool.size());
            MerchantOffer offer = pool.get(roll).apply(context);
            if (offer == null || !set.allowDuplicates()) pool.remove(roll);
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
        return true;
    }

    /** A 1.20.1 {@code ItemListing.getOffer(trader, random)}, or {@code null} if it can't make one. */
    public static MerchantOffer offer(Object listing, Entity trader, RandomSource random) {
        if (listing instanceof BasicItemListing basic) return basic.getOffer(trader, random);
        try {
            Method getOffer = listing.getClass().getMethod("getOffer", Entity.class, RandomSource.class);
            return (MerchantOffer) getOffer.invoke(listing, trader, random);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private ForgeTrades() {}
}
