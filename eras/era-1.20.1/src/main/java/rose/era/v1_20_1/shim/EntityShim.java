package rose.era.v1_20_1.shim;

import java.util.function.Consumer;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/** Redirect targets for 1.20.1 entity and item-durability calls that 26.3 removed or reshaped. */
public final class EntityShim {
    /** 1.20.1 {@code displayClientMessage(message, actionBar)}; 26.3 split it into overlay and system messages. */
    public static void displayClientMessage(Player player, Component message, boolean actionBar) {
        if (actionBar) player.sendOverlayMessage(message);
        else player.sendSystemMessage(message);
    }

    /** 1.20.1 {@code Player.drop(stack, includeThrowerName)}: a server-side drop in front of the player. */
    public static ItemEntity drop(Player player, ItemStack stack, boolean includeThrowerName) {
        return player.drop(stack, false, Prediction.SERVER_ONLY);
    }

    /** 1.20.1 {@code Entity.hurt(source, amount)}; 1.21.2 split it into hurtServer and hurtClient. */
    public static boolean hurt(Entity entity, DamageSource source, float amount) {
        if (entity.level() instanceof ServerLevel level) return entity.hurtServer(level, source, amount);
        return entity.hurtClient(source);
    }

    /** 1.20.1 {@code getCommandSenderWorld()}; 26.3 has {@code level()}. */
    public static Level getCommandSenderWorld(Entity entity) {
        return entity.level();
    }

    /** 1.20.1 {@code broadcastBreakEvent(slot)}: the item-break animation; 26.3 plays it in onEquippedItemBroken. */
    public static void broadcastBreakEvent(LivingEntity entity, EquipmentSlot slot) {
        entity.onEquippedItemBroken(ItemStack.EMPTY, slot);
    }

    public static void broadcastBreakEvent(LivingEntity entity, InteractionHand hand) {
        broadcastBreakEvent(entity, hand.asEquipmentSlot());
    }

    /** 1.20.1 {@code stack.hurtAndBreak(amount, owner, onBroken)}: onBroken got the owner, the stack shrank. */
    public static <T extends LivingEntity> void hurtAndBreak(ItemStack stack, int amount, T owner, Consumer<T> onBroken) {
        if (owner.level() instanceof ServerLevel level) {
            stack.hurtAndBreak(amount, level, owner instanceof ServerPlayer player ? player : null, broken -> onBroken.accept(owner));
        }
    }

    /** 1.20.1 {@code stack.hurt(amount, random, player)}: damages the item and returns whether it broke (no shrink). */
    public static boolean hurt(ItemStack stack, int amount, RandomSource random, ServerPlayer player) {
        if (!stack.isDamageableItem()) return false;
        if (amount > 0 && player != null) amount = EnchantmentHelper.processDurabilityChange(player.level(), stack, amount);
        if (amount <= 0) return false;
        int damage = stack.getDamageValue() + amount;
        if (player != null) CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, stack, damage);
        stack.setDamageValue(damage);
        return damage >= stack.getMaxDamage();
    }

    private EntityShim() {}

    /** 1.20.1 {@code causeFallDamage(float distance, float multiplier, source)}; 1.21.5 made the distance a double. */
    public static boolean causeFallDamage(Entity self, float fallDistance, float multiplier, DamageSource source) {
        return self.causeFallDamage(fallDistance, multiplier, source);
    }

    /** 1.20.1 {@code PiglinAi.angerNearbyPiglins(player, onlyIfSeen)}; 26.3 needs the server level (no effect on clients). */
    public static void angerNearbyPiglins(Player player, boolean onlyIfTheySeeThePlayer) {
        if (player.level() instanceof ServerLevel level) {
            net.minecraft.world.entity.monster.piglin.PiglinAi.angerNearbyPiglins(level, player, onlyIfTheySeeThePlayer);
        }
    }
}
