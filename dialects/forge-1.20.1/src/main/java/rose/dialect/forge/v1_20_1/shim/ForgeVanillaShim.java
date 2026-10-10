package rose.dialect.forge.v1_20_1.shim;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Redirect targets for methods Forge 1.20.1 added to vanilla classes (IForge* extensions, patches). */
public final class ForgeVanillaShim {
    /**
     * Forge {@code MobEffectInstance.isCurativeItem(stack)}: whether the stack cures the effect. Forge's default cure
     * is a milk bucket ({@code IForgeMobEffect.getCurativeItems()}); a mod effect lists its own by overriding
     * {@code getCurativeItems()}.
     */
    public static boolean isCurativeItem(MobEffectInstance self, ItemStack stack) {
        return curativeItems(self.getEffect().value()).stream().anyMatch(cure -> ItemStack.isSameItem(cure, stack));
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> curativeItems(MobEffect effect) {
        try {
            return (List<ItemStack>) effect.getClass().getMethod("getCurativeItems").invoke(effect);
        } catch (NoSuchMethodException e) {
            return List.of(new ItemStack(Items.MILK_BUCKET));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("getCurativeItems of " + effect.getClass().getName() + " failed", e);
        }
    }

    /**
     * Forge {@code ItemStack.getFoodProperties(entity)}: the item's {@code IForgeItem.getFoodProperties(stack, entity)}
     * if it overrides it, else the stack's food (1.20.1 {@code Item.getFoodProperties()}; 26.3: the food component).
     */
    public static FoodProperties getFoodProperties(ItemStack self, LivingEntity entity) {
        try {
            Method override = self.getItem().getClass().getMethod("getFoodProperties", ItemStack.class, LivingEntity.class);
            return (FoodProperties) override.invoke(self.getItem(), self, entity);
        } catch (NoSuchMethodException e) {
            return self.get(DataComponents.FOOD);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("getFoodProperties of " + self.getItem().getClass().getName() + " failed", e);
        }
    }

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
