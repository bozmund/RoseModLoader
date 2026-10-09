package net.minecraftforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

/** Helpers Forge mods call to fire Forge hooks from their own code. */
public final class ForgeHooks {
    /** Asks listeners whether a crop may grow now; {@code def} is the mod's own answer. */
    public static boolean onCropsGrowPre(Level level, BlockPos pos, BlockState state, boolean def) {
        BlockEvent.CropGrowEvent.Pre event = new BlockEvent.CropGrowEvent.Pre(level, pos, state);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.ALLOW || (event.getResult() == Event.Result.DEFAULT && def);
    }

    public static void onCropsGrowPost(Level level, BlockPos pos, BlockState state) {
        MinecraftForge.EVENT_BUS.post(new BlockEvent.CropGrowEvent.Post(level, pos, state, level.getBlockState(pos)));
    }

    private ForgeHooks() {}
}
