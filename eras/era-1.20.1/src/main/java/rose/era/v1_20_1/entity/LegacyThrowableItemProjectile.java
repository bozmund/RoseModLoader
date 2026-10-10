package rose.era.v1_20_1.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Era bridge (1.20.1): ThrowableItemProjectile's 1.20.1 constructors. 1.21.2 made the thrown stack a constructor
 * argument; 1.20.1 code passes none and calls setItem afterwards (until then the projectile shows its default item).
 */
public abstract class LegacyThrowableItemProjectile extends ThrowableItemProjectile {
    protected LegacyThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    protected LegacyThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level, ItemStack.EMPTY);
        this.setItem(new ItemStack(this.getDefaultItem()));
    }

    protected LegacyThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner, Level level) {
        super(type, owner, level, ItemStack.EMPTY);
        this.setItem(new ItemStack(this.getDefaultItem()));
    }
}
