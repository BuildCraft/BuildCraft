package buildcraft.factory.gui;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.factory.TileHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerHopper extends BuildCraftContainer {

	IInventory playerIInventory;
	TileHopper hopper;

	public ContainerHopper(InventoryPlayer inventory, TileHopper tile) {
		super(tile.getSizeInventory());
		playerIInventory = inventory;
		hopper = tile;

		// Adding hopper inventory
		addSlotToContainer(new Slot(tile, 0, 62, 18));
		addSlotToContainer(new Slot(tile, 1, 80, 18));
		addSlotToContainer(new Slot(tile, 2, 98, 18));
		addSlotToContainer(new Slot(tile, 3, 80, 36));

		// Player inventory
		for (int i1 = 0; i1 < 3; i1++) {
			for (int l1 = 0; l1 < 9; l1++) {
				addSlotToContainer(new Slot(inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 71 + i1 * 18));
			}
		}

		// Player hotbar
		for (int j1 = 0; j1 < 9; j1++) {
			addSlotToContainer(new Slot(inventory, j1, 8 + j1 * 18, 129));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return hopper.isUseableByPlayer(entityPlayer);
	}
}
