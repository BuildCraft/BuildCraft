/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.silicon.TileStampingTable;

public class ContainerStampingTable extends BuildCraftContainer {

	private TileStampingTable table;

	public ContainerStampingTable(InventoryPlayer playerInventory, TileStampingTable table) {
		super(table.getSizeInventory());
		this.table = table;

		addSlot(new SlotValidated(table, 0, 15, 18));
		addSlot(new SlotOutput(table, 1, 143, 18));
		addSlot(new SlotOutput(table, 2, 111, 45));
		addSlot(new SlotOutput(table, 3, 129, 45));
		addSlot(new SlotOutput(table, 4, 147, 45));

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 69 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 127));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return table.isUseableByPlayer(var1);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (Object crafter : crafters) {
			table.sendGUINetworkData(this, (ICrafting) crafter);
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2) {
		table.getGUINetworkData(par1, par2);
	}
}
