package net.minecraftforge.event;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * A creative tab is filling its contents (mod bus): mods add their items to any tab, vanilla's included. Fired after the
 * tab's own items, as Forge's CreativeModeTab.buildContents patch did.
 */
public final class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {
    private final CreativeModeTab tab;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final CreativeModeTab.Output output;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        this.tab = tab;
        this.tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
        this.parameters = parameters;
        this.output = output;
    }

    public CreativeModeTab getTab() {
        return tab;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return tabKey;
    }

    public FeatureFlagSet getFlags() {
        return parameters.enabledFeatures();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return parameters;
    }

    public boolean hasPermissions() {
        return parameters.hasPermissions();
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        output.accept(stack, visibility);
    }

    public void accept(Supplier<? extends ItemLike> item, CreativeModeTab.TabVisibility visibility) {
        accept(item.get(), visibility);
    }

    public void accept(Supplier<? extends ItemLike> item) {
        accept(item.get());
    }
}
