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

    private VillagerShim() {}

    /** 1.20.1 {@code VillagerProfession.name()}: the profession's id path ("farmer"); 26.3's name() is its display name. */
    public static String name(net.minecraft.world.entity.npc.villager.VillagerProfession self) {
        var id = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.getKey(self);
        return id == null ? "none" : id.getPath();
    }
}
