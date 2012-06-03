package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;

public class ContainerHopper extends BuildCraftContainer {

	IInventory playerIInventory;
	TileHopper hopper;
	
	public ContainerHopper(InventoryPlayer inventory, TileHopper tile) {
		super(tile);
		playerIInventory = inventory;
		hopper = tile;
		
		//Adding hopper inventory
		addSlot(new Slot(tile, 3, 62, 18));
		addSlot(new Slot(tile, 2, 80, 18));
		addSlot(new Slot(tile, 1, 98, 18));
		addSlot(new Slot(tile, 0, 80, 36));

		//Player inventory
		for (int i1 = 0; i1 < 3; i1++)
			for (int l1 = 0; l1 < 9; l1++) {
				addSlot(new Slot(inventory, l1 + i1 * 9 + 9, 8 + l1 * 18,
						71 + i1 * 18));
			}

		//Player hotbar
		for (int j1 = 0; j1 < 9; j1++)
			addSlot(new Slot(inventory, j1, 8 + j1 * 18, 129));
		
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return hopper.isUseableByPlayer(entityPlayer);
	}
}
