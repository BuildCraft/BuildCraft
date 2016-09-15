package buildcraft.silicon.container;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
    private static final int PLAYER_INV_START = 109;
    public ContainerIntegrationTable(EntityPlayer player, TileIntegrationTable tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate, (x == 1 && y == 1) ? 0 : (y > 1 || (x == 2 && y == 1)) ? x + y * 3 - 1 : x + y * 3, 19 + x * 25, 24 + y * 25));
            }
        }

        addSlotToContainer(new SlotOutput(tile.invResult, 0, 138, 49));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
