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
import buildcraft.core.lib.gui.slots.SlotPhantom;
import buildcraft.core.lib.gui.slots.SlotUntouchable;
import buildcraft.silicon.TileAdvancedCraftingTable;

public class ContainerAdvancedCraftingTable extends BuildCraftContainer {

	private TileAdvancedCraftingTable workbench;

	public ContainerAdvancedCraftingTable(InventoryPlayer playerInventory, TileAdvancedCraftingTable table) {
		super(table.getSizeInventory());
		this.workbench = table;

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				addSlotToContainer(new SlotPhantom(table.getCraftingSlots(), x + y * 3, 33 + x * 18, 16 + y * 18));
			}
		}

		addSlotToContainer(new SlotUntouchable(table.getOutputSlot(), 0, 127, 34));

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 5; x++) {
				addSlotToContainer(new Slot(workbench, x + y * 5, 15 + x * 18, 85 + y * 18));
			}
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				addSlotToContainer(new SlotOutput(workbench, 15 + x + y * 3, 109 + x * 18, 85 + y * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 153 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return workbench.isUseableByPlayer(var1);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (Object crafter : crafters) {
			workbench.sendGUINetworkData(this, (ICrafting) crafter);
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2) {
		workbench.getGUINetworkData(par1, par2);
	}
}
