package net.minecraftforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/** Forge let mods keep farmland wet without water nearby ("water tickets"). Rose has no tickets yet. */
public final class FarmlandWaterManager {
    public static boolean hasBlockWaterTicket(LevelReader level, BlockPos pos) {
        return false;
    }

    private FarmlandWaterManager() {}
}
