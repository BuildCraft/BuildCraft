package buildcraft.silicon.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.silicon.tile.TileIntegrationTable;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
    private static final int PLAYER_INV_START = 109;
    public ContainerIntegrationTable(EntityPlayer player, TileIntegrationTable tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        int[] indexes = {0, 1, 2, 3, 0, 4, 5, 6, 7};

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate, indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
            }
        }

        addSlotToContainer(new SlotOutput(tile.invResult, 0, 138, 49));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
