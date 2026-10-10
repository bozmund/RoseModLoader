package rose.era.v1_20_1.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Era bridge (1.20.1): container block entities (chests and the like) saved to and loaded from a CompoundTag, as
 * {@link LegacyBlockEntity} does for plain block entities. Lock and custom name are still read and written by 26.3;
 * the mod's own data (items, loot table) goes through its 1.20.1 overrides.
 */
public abstract class LegacyRandomizableContainerBlockEntity extends RandomizableContainerBlockEntity {
    protected LegacyRandomizableContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 1.20.1: write this block entity's data into {@code tag}. Subclasses override and call super. */
    protected void saveAdditional(CompoundTag tag) {
    }

    /** 1.20.1 {@code load(tag)}, under 26.3's name for the same method (see LegacyBlockEntity). */
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

    /** 1.20.1: read the loot table this container still has to fill itself from; true if there is one. */
    protected boolean tryLoadLootTable(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("LootTable", ""));
        if (id == null || !tag.contains("LootTable")) return false;
        this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, id);
        this.lootTableSeed = tag.getLongOr("LootTableSeed", 0L);
        return true;
    }

    /** 1.20.1: write the pending loot table; true if there is one (then the items aren't saved). */
    protected boolean trySaveLootTable(CompoundTag tag) {
        if (this.lootTable == null) return false;
        tag.putString("LootTable", this.lootTable.identifier().toString());
        if (this.lootTableSeed != 0L) tag.putLong("LootTableSeed", this.lootTableSeed);
        return true;
    }

    /** 1.20.1 {@code setCustomName(name)} (a renamed item placed as this block); 26.3 sets it from components. */
    public void setCustomName(Component name) {
        this.name = name;
    }
}
