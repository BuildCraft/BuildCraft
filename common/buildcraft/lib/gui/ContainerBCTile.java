package buildcraft.lib.gui;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune {
    public final T tile;

    public ContainerBCTile(EntityPlayer player, T tile) {
        super(player);
        this.tile = tile;
        tile.onPlayerOpen(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }

    @Override
    public void detectAndSendChanges() {
        tile.sendNetworkGuiTick();
    }
}
