package buildcraft.silicon.container;

import buildcraft.lib.gui.slot.SlotDisplay;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.item.ItemStack;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
    public ContainerIntegrationTable(EntityPlayer player, TileIntegrationTable tile) {
        super(player, tile);
        addFullPlayerInventory(109);

        int[] indexes = {0, 1, 2, 3, 0, 4, 5, 6, 7};

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate, indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
            }
        }

        addSlotToContainer(new SlotDisplay(i -> tile.recipe != null ? tile.recipe.output : ItemStack.EMPTY, 0, 101, 36));

        addSlotToContainer(new SlotOutput(tile.invResult, 0, 138, 49));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
