package buildcraft.factory.gui;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotOutput;
import buildcraft.core.gui.slots.SlotValidated;
import buildcraft.factory.TileFluidicCompressor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerFluidicCompressor extends BuildCraftContainer {
	
	IInventory playerIInventory;
	TileFluidicCompressor canner;

	public ContainerFluidicCompressor(InventoryPlayer inventory, TileFluidicCompressor tile) {
		super(tile.getSizeInventory());
		playerIInventory = inventory;
		canner = tile;
		
		this.addSlotToContainer(new SlotValidated(tile, 0, 89, 31));
		this.addSlotToContainer(new SlotOutput (tile, 1, 126, 35));
		
		for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex)
        {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex)
            {
                this.addSlotToContainer(new Slot(inventory, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 84 + inventoryRowIndex * 18));
            }
        }
		for (int hotbbarIndex = 0; hotbbarIndex < 9; ++hotbbarIndex)
        {
            this.addSlotToContainer(new Slot(inventory, hotbbarIndex, 8 + hotbbarIndex * 18, 142));
        }
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return canner.isUseableByPlayer(entityPlayer);
	}

}
