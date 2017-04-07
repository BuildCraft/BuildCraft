package buildcraft.factory.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.factory.tile.TileChute;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

public class ContainerChute extends ContainerBCTile<TileChute> {
    public ContainerChute(EntityPlayer player, TileChute tile) {
        super(player, tile);
        addFullPlayerInventory(71);

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
