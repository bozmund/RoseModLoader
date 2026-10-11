package net.minecraftforge.event.entity.living;

import java.util.Collection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/** A dying entity's drops, before they are spawned; listeners may change them, canceling drops nothing. */
@Cancelable
public class LivingDropsEvent extends LivingEvent {
    private final DamageSource source;
    private final Collection<ItemEntity> drops;
    private final int lootingLevel;
    private final boolean recentlyHit;

    public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
        super(entity);
        this.source = source;
        this.drops = drops;
        this.lootingLevel = lootingLevel;
        this.recentlyHit = recentlyHit;
    }

    public DamageSource getSource() {
        return source;
    }

    public Collection<ItemEntity> getDrops() {
        return drops;
    }

    /** 0: 26.3 applies looting through enchantment effects inside loot tables. */
    public int getLootingLevel() {
        return lootingLevel;
    }

    public boolean isRecentlyHit() {
        return recentlyHit;
    }
}
