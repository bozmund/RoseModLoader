package rose.dialect.forge.v1_20_1.shim;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Redirect targets for methods Forge 1.20.1 added to vanilla classes (IForge* extensions, patches). */
public final class ForgeVanillaShim {
    /** Forge {@code BlockTags.create(id)}. */
    public static TagKey<Block> blockTag(Identifier id) {
        return TagKey.create(Registries.BLOCK, id);
    }

    /** Forge {@code ItemTags.create(id)}. */
    public static TagKey<Item> itemTag(Identifier id) {
        return TagKey.create(Registries.ITEM, id);
    }

    /** Forge {@code Properties.lootFrom(block)}: drop what another block drops (resolved when this block is built). */
    public static BlockBehaviour.Properties lootFrom(BlockBehaviour.Properties self, Supplier<? extends Block> blockIn) {
        self.drops = id -> blockIn.get().getLootTable();
        return self;
    }

    /**
     * Forge {@code CreativeModeTab.builder()}: mod tabs had no fixed slot (Forge paginated them). Rose puts them
     * after vanilla's tabs; see the creative tab placement in Rose core.
     */
    public static CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, NEXT_COLUMN.getAndIncrement());
    }

    /** Past vanilla's tabs, so positions stay unique (vanilla validates that). */
    private static final java.util.concurrent.atomic.AtomicInteger NEXT_COLUMN = new java.util.concurrent.atomic.AtomicInteger(100);

    /** Forge {@code ItemStack.getCraftingRemainingItem()}: what stays in the grid after crafting (26.3: a template). */
    public static net.minecraft.world.item.ItemStack craftingRemainingItem(net.minecraft.world.item.ItemStack self) {
        var template = self.getItem().getCraftingRemainder();
        return template == null ? net.minecraft.world.item.ItemStack.EMPTY : template.create();
    }

    /** Forge {@code ItemStack.hasCraftingRemainingItem()}. */
    public static boolean hasCraftingRemainingItem(net.minecraft.world.item.ItemStack self) {
        return self.getItem().getCraftingRemainder() != null;
    }

    /**
     * Forge {@code ItemStack.canPerformAction(action)}: mod items answer through their own
     * {@code canPerformAction(ItemStack, ToolAction)} override; vanilla items by their tool tags.
     */
    public static boolean canPerformAction(net.minecraft.world.item.ItemStack self, net.minecraftforge.common.ToolAction action) {
        var item = self.getItem();
        var own = rose.era.v1_20_1.bridge.Legacy.find(item, "canPerformAction", java.lang.invoke.MethodType.methodType(
                boolean.class, net.minecraft.world.item.ItemStack.class, net.minecraftforge.common.ToolAction.class));
        if (own.isPresent()) return (Boolean) rose.era.v1_20_1.bridge.Legacy.invoke(own.get(), item, self, action);
        if (self.is(net.minecraft.tags.ItemTags.AXES)) return net.minecraftforge.common.ToolActions.DEFAULT_AXE_ACTIONS.contains(action);
        if (self.is(net.minecraft.tags.ItemTags.PICKAXES)) return net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action);
        if (self.is(net.minecraft.tags.ItemTags.SHOVELS)) return net.minecraftforge.common.ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action);
        if (self.is(net.minecraft.tags.ItemTags.HOES)) return net.minecraftforge.common.ToolActions.DEFAULT_HOE_ACTIONS.contains(action);
        if (self.is(net.minecraft.tags.ItemTags.SWORDS)) return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(action);
        if (self.is(net.minecraft.world.item.Items.SHEARS)) return net.minecraftforge.common.ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action);
        return false;
    }

    private ForgeVanillaShim() {}
}
