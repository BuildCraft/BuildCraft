package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileRefineryControl;

public class ContainerRefineryControl extends BuildCraftContainer{
	
	IInventory playerIInventory;
	TileRefineryControl refineryControl;

public ContainerRefineryControl(InventoryPlayer inventory, TileRefineryControl tile) {
		super(tile.getSizeInventory());
	}
	

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return true;
	}

}
