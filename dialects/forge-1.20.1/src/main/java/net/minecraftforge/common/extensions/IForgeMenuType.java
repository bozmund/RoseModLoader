package net.minecraftforge.common.extensions;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;

/** Forge's extension of MenuType: menu types whose client side receives extra data. */
public interface IForgeMenuType<T> {
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.VANILLA_SET);
    }
}
