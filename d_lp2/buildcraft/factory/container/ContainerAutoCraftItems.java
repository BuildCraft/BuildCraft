package buildcraft.factory.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.gui.ContainerBC8;

public class ContainerAutoCraftItems extends ContainerBC8 {
    private static final int PLAYER_INV_START = 115;

    public final TileAutoWorkbenchItems tile;

    public ContainerAutoCraftItems(EntityPlayer player, TileAutoWorkbenchItems tile) {
        super(player);
        this.tile = tile;
        addFullPlayerInventory(PLAYER_INV_START);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
