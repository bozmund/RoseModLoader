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

    private VillagerShim() {}
}
