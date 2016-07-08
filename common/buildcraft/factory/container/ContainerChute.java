package buildcraft.factory.container;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.gui.ContainerBCTile;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerChute extends ContainerBCTile<TileChute> {
    private static final int PLAYER_INV_START = 71;

    public ContainerChute(EntityPlayer player, TileChute tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        addSlotToContainer(new SlotBase(tile.inv, 0, 62, 18));
        addSlotToContainer(new SlotBase(tile.inv, 1, 80, 18));
        addSlotToContainer(new SlotBase(tile.inv, 2, 98, 18));
        addSlotToContainer(new SlotBase(tile.inv, 3, 80, 36));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
