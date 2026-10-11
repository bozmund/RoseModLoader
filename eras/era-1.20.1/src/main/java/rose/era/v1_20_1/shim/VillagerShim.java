package rose.era.v1_20_1.shim;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/** Redirect targets for 1.20.1 villager APIs: professions became registry holders and keys in 26.x. */
public final class VillagerShim {
    /** 1.20.1 {@code VillagerData.getProfession()}: 26.3 {@code profession()} is a {@code Holder}. */
    public static VillagerProfession getProfession(VillagerData self) {
        return self.profession().value();
    }

    /** 1.20.1 {@code VillagerProfession.FARMER} (the profession): 26.3 {@code FARMER} is a {@code ResourceKey}. */
    public static VillagerProfession FARMER() {
        return BuiltInRegistries.VILLAGER_PROFESSION.getValueOrThrow(VillagerProfession.FARMER);
    }

    /**
     * 1.20.1 {@code TradeWithVillager.throwHalfStack(villager, items, target)} (reached through Mixin invokers): 26.3
     * picks the stack to throw with a predicate instead of an item set.
     */
    public static void throwHalfStack(net.minecraft.world.entity.npc.villager.Villager villager, java.util.Set<net.minecraft.world.item.Item> items,
                                      net.minecraft.world.entity.LivingEntity target) {
        net.minecraft.world.entity.ai.behavior.TradeWithVillager.throwHalfStack(villager, stack -> items.contains(stack.getItem()), target);
    }

    // 1.20.1 new MerchantOffer(costA, [costB,] result, [uses,] maxUses, xp, priceMultiplier[, demand]) took ItemStack
    // costs; 1.20.5 made them ItemCosts (item and count; an empty second cost is none).

    public static net.minecraft.world.item.trading.MerchantOffer merchantOffer(net.minecraft.world.item.ItemStack costA, net.minecraft.world.item.ItemStack result,
                                                                              int maxUses, int xp, float priceMultiplier) {
        return merchantOffer(costA, net.minecraft.world.item.ItemStack.EMPTY, result, 0, maxUses, xp, priceMultiplier, 0);
    }

    public static net.minecraft.world.item.trading.MerchantOffer merchantOffer(net.minecraft.world.item.ItemStack costA, net.minecraft.world.item.ItemStack costB,
                                                                              net.minecraft.world.item.ItemStack result, int maxUses, int xp, float priceMultiplier) {
        return merchantOffer(costA, costB, result, 0, maxUses, xp, priceMultiplier, 0);
    }

    public static net.minecraft.world.item.trading.MerchantOffer merchantOffer(net.minecraft.world.item.ItemStack costA, net.minecraft.world.item.ItemStack costB,
                                                                              net.minecraft.world.item.ItemStack result, int uses, int maxUses, int xp, float priceMultiplier) {
        return merchantOffer(costA, costB, result, uses, maxUses, xp, priceMultiplier, 0);
    }

    public static net.minecraft.world.item.trading.MerchantOffer merchantOffer(net.minecraft.world.item.ItemStack costA, net.minecraft.world.item.ItemStack costB,
                                                                              net.minecraft.world.item.ItemStack result, int uses, int maxUses, int xp, float priceMultiplier, int demand) {
        var second = costB.isEmpty() ? java.util.Optional.<net.minecraft.world.item.trading.ItemCost>empty()
                : java.util.Optional.of(new net.minecraft.world.item.trading.ItemCost(costB.getItem(), costB.getCount()));
        return new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(costA.getItem(), costA.getCount()),
                second, result, uses, maxUses, xp, priceMultiplier, demand);
    }

    private VillagerShim() {}

    /** 1.20.1 {@code VillagerProfession.name()}: the profession's id path ("farmer"); 26.3's name() is its display name. */
    public static String name(net.minecraft.world.entity.npc.villager.VillagerProfession self) {
        var id = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.getKey(self);
        return id == null ? "none" : id.getPath();
    }
}
