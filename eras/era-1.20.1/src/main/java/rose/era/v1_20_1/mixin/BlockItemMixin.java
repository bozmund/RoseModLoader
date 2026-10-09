package rose.era.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.20.1 placed block items with a {@code BlockEntityTag} (in the item's tag) loaded it into the new block entity;
 * 1.20.5 moved this to the {@code block_entity_data} component. Old mods still write {@code BlockEntityTag}, which
 * Rose keeps in {@code custom_data} (ItemNbtShim), so it is applied here as 1.20.1 did: merged into the block
 * entity's data, then loaded.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "updateCustomBlockEntityTag", at = @At("RETURN"), cancellable = true)
    private static void rose$legacyBlockEntityTag(Level level, Player player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide() || cir.getReturnValueZ()) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !(data.copyTag().get("BlockEntityTag") instanceof CompoundTag legacy)) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.getType().onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) return;
        CompoundTag current = blockEntity.saveWithoutMetadata(level.registryAccess());
        CompoundTag merged = current.copy().merge(legacy);
        if (merged.equals(current)) return;
        blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), merged));
        blockEntity.setChanged();
        cir.setReturnValue(true);
    }
}
