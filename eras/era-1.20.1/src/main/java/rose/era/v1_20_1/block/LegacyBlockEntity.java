package rose.era.v1_20_1.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import rose.era.v1_20_1.EraContext;

/**
 * Era bridge (1.20.1): block entities saved to and loaded from a CompoundTag ({@code saveAdditional(tag)},
 * {@code load(tag)}); 26.3 writes through ValueOutput and reads ValueInput. Mod block entities extending
 * BlockEntity are re-parented here, so their old overrides (and their super calls) keep working.
 */
public abstract class LegacyBlockEntity extends BlockEntity {
    protected LegacyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 1.20.1: write this block entity's data into {@code tag}. Subclasses override and call super. */
    protected void saveAdditional(CompoundTag tag) {
    }

    /**
     * 1.20.1 {@code load(tag)}: read this block entity's data. Translated mods override it under 26.3's name for the
     * same method ({@code m_142466_} became {@code loadAdditional}); they call super under that name too.
     */
    public void loadAdditional(CompoundTag tag) {
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        for (String key : tag.keySet()) output.store(key, ExtraCodecs.NBT, tag.get(key));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        loadAdditional(input instanceof TagValueInput tagInput ? tagInput.input : new CompoundTag());
    }

    /** 1.20.1 {@code getUpdateTag()}: data sent to clients when the chunk loads (empty unless overridden). */
    public CompoundTag getUpdateTag() {
        return new CompoundTag();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return getUpdateTag();
    }

    /** 1.20.1 {@code saveWithoutMetadata()}: the full data without id and position. */
    public CompoundTag saveWithoutMetadata() {
        return saveWithoutMetadata(level != null ? level.registryAccess() : EraContext.registryAccess());
    }
}
