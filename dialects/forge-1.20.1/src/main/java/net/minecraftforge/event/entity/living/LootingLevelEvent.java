package net.minecraftforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** The looting level a kill's loot is rolled with; listeners may change it. */
public class LootingLevelEvent extends LivingEvent {
    private final @Nullable DamageSource damageSource;
    private int lootingLevel;

    public LootingLevelEvent(LivingEntity entity, @Nullable DamageSource damageSource, int lootingLevel) {
        super(entity);
        this.damageSource = damageSource;
        this.lootingLevel = lootingLevel;
    }

    public @Nullable DamageSource getDamageSource() {
        return damageSource;
    }

    public int getLootingLevel() {
        return lootingLevel;
    }

    public void setLootingLevel(int lootingLevel) {
        this.lootingLevel = lootingLevel;
    }
}
