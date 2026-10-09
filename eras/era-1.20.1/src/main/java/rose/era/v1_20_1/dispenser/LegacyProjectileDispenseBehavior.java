package rose.era.v1_20_1.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * 1.20.1 {@code AbstractProjectileDispenseBehavior}: subclasses create the projectile, this shoots it. 1.20.5 made
 * {@code ProjectileDispenseBehavior} take a {@code ProjectileItem} instead; mod subclasses are rebased onto this.
 */
public abstract class LegacyProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Position position = DispenserBlock.getDispensePosition(source);
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Projectile projectile = getProjectile(level, position, stack);
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1F, direction.getStepZ(), getPower(), getUncertainty());
        level.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }

    protected abstract Projectile getProjectile(Level level, Position position, ItemStack stack);

    protected float getUncertainty() {
        return 6.0F;
    }

    protected float getPower() {
        return 1.1F;
    }
}
