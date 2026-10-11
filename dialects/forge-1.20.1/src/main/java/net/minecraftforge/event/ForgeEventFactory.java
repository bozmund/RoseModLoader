package net.minecraftforge.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

/** Helpers that fire Forge events from mod code. */
public final class ForgeEventFactory {
    public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, item, duration, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResultStack();
    }

    /** Whether mobs may change blocks here (the mobGriefing game rule; Forge let listeners override it). */
    public static boolean getMobGriefingEvent(Level level, Entity entity) {
        return level instanceof ServerLevel server && server.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    private ForgeEventFactory() {}

    public static net.minecraft.world.entity.player.Player.BedSleepingProblem onPlayerSleepInBed(net.minecraft.world.entity.player.Player player,
            java.util.Optional<net.minecraft.core.BlockPos> pos) {
        return rose.dialect.forge.v1_20_1.SleepEvents.sleepInBed(player, pos);
    }

    /** Whether the player may sleep now; DEFAULT is vanilla's 1.20.1 test, that it is night (not day). */
    public static boolean fireSleepingTimeCheck(net.minecraft.world.entity.player.Player player, java.util.Optional<net.minecraft.core.BlockPos> pos) {
        return rose.dialect.forge.v1_20_1.SleepEvents.sleepingTime(player, pos, !player.level().isBrightOutside());
    }
}
