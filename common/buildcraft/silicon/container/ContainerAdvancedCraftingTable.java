package buildcraft.silicon.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;

public class ContainerAdvancedCraftingTable extends ContainerBCTile<TileAdvancedCraftingTable> {
    public ContainerAdvancedCraftingTable(EntityPlayer player, TileAdvancedCraftingTable tile) {
        super(player, tile);
        addFullPlayerInventory(153);

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18));
            }
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 5; x++) {
                addSlotToContainer(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotBase(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
