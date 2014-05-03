/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotOutput;
import buildcraft.core.gui.slots.SlotUntouchable;
import buildcraft.core.gui.slots.SlotValidated;
import buildcraft.silicon.TileIntegrationTable;

public class ContainerIntegrationTable extends BuildCraftContainer {

	private TileIntegrationTable table;

	public ContainerIntegrationTable(InventoryPlayer playerInventory, TileIntegrationTable table) {
		super(table.getSizeInventory());
		this.table = table;

		addSlot(new SlotValidated(table, TileIntegrationTable.SLOT_INPUT_A, 17, 28));
		addSlot(new SlotValidated(table, TileIntegrationTable.SLOT_INPUT_B, 53, 28));
		addSlot(new SlotOutput(table, TileIntegrationTable.SLOT_OUTPUT, 143, 44));
		addSlot(new SlotUntouchable(table.getRecipeOutput(), 0, 116, 44));

		for (int i = TileIntegrationTable.SLOT_OUTPUT + 1; i < 12; i++) {
			addSlot(new Slot(table, i, 8 + (i - (TileIntegrationTable.SLOT_OUTPUT + 1)) * 18, 69));
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 91 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 149));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return table.isUseableByPlayer(var1);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++) {
			table.sendGUINetworkData(this, (ICrafting) crafters.get(i));
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2) {
		table.getGUINetworkData(par1, par2);
	}
}
