package rose.era.v1_20_1.block;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Mod block entities that extend a vanilla one (a mod sign is a SignBlockEntity, a mod chest a ChestBlockEntity) store
 * that vanilla block entity's data, in the format of the version that saved it. Vanilla upgrades its own block
 * entities in old chunks with data fixers by id; here mod ones are given the vanilla id for that upgrade
 * (SimpleRegionStorageMixin), so a 1.20.1 world's mod signs get their text in 26.x's format, and so on.
 */
public final class LegacyBlockEntities {
    private static final String MOD_ID = "rose:mod_id";
    private static final Map<BlockEntityType<?>, Optional<Identifier>> VANILLA_BASE = new ConcurrentHashMap<>();

    /** Before the chunk's data fixes: mod block entities with a vanilla base take its id. */
    public static void asVanilla(CompoundTag chunk) {
        chunk.getListOrEmpty("block_entities").forEach(t -> {
            if (!(t instanceof CompoundTag entity)) return;
            Identifier id = Identifier.tryParse(entity.getStringOr("id", ""));
            if (id == null || id.getNamespace().equals("minecraft")) return;
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id);
            if (type == null) return;
            vanillaBase(type).ifPresent(vanilla -> {
                entity.putString(MOD_ID, id.toString());
                entity.putString("id", vanilla.toString());
            });
        });
    }

    /** After the chunk's data fixes: the mod ids come back. */
    public static void restore(CompoundTag chunk) {
        chunk.getListOrEmpty("block_entities").forEach(t -> {
            if (t instanceof CompoundTag entity && entity.get(MOD_ID) instanceof Tag) {
                entity.putString("id", entity.getStringOr(MOD_ID, ""));
                entity.remove(MOD_ID);
            }
        });
    }

    /** The id of the vanilla block entity whose class {@code type}'s block entities extend, if any. */
    public static Optional<Identifier> vanillaBase(BlockEntityType<?> type) {
        return VANILLA_BASE.computeIfAbsent(type, t -> {
            Class<?> vanilla = vanillaClass(t);
            if (vanilla == null) return Optional.empty();
            for (BlockEntityType<?> candidate : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                Identifier key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(candidate);
                if (key == null || !key.getNamespace().equals("minecraft")) continue;
                BlockEntity sample = sample(candidate);
                if (sample != null && sample.getClass() == vanilla) return Optional.of(key);
            }
            return Optional.empty();
        });
    }

    /** The nearest vanilla class a mod block entity type's block entities extend, other than BlockEntity itself. */
    public static @Nullable Class<?> vanillaClass(BlockEntityType<?> type) {
        BlockEntity sample = sample(type);
        if (sample == null) return null;
        Class<?> vanilla = sample.getClass();
        while (vanilla != null && !vanilla.getName().startsWith("net.minecraft.")) vanilla = vanilla.getSuperclass();
        return vanilla == null || vanilla == BlockEntity.class ? null : vanilla;
    }

    /** A block entity of this type, made for any block it accepts; {@code null} if none can be made. */
    public static @Nullable BlockEntity sample(BlockEntityType<?> type) {
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            if (!type.isValid(state)) continue;
            try {
                return type.create(BlockPos.ZERO, state);
            } catch (RuntimeException | LinkageError e) {
                return null;
            }
        }
        return null;
    }

    private LegacyBlockEntities() {}
}
