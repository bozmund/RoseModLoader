package net.minecraftforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Creates a menu on the client from the extra data the server sent with NetworkHooks.openScreen. */
public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
    T create(int windowId, Inventory inv, FriendlyByteBuf data);

    @Override
    default T create(int windowId, Inventory inv) {
        return create(windowId, inv, rose.dialect.forge.v1_20_1.MenuData.take(windowId));
    }
}
