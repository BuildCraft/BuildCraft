package buildcraft.factory.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.gui.ContainerBCTile;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    private static final int PLAYER_INV_START = 115;

    public ContainerAutoCraftItems(EntityPlayer player, TileAutoWorkbenchItems tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        if (!tile.getWorld().isRemote) {
            tile.deltaProgress.addDelta(0, 200, 100);
            tile.deltaProgress.addDelta(200, 210, -100);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
