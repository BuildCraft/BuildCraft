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
import buildcraft.core.lib.gui.slots.SlotUntouchable;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.silicon.TileIntegrationTable;

public class ContainerIntegrationTable extends BuildCraftContainer {
	public static final int[] SLOT_X = {
			44, 44, 69, 69, 69, 44, 19, 19, 19
	};
	public static final int[] SLOT_Y = {
			49, 24, 24, 49, 74, 74, 74, 49, 24
	};

	private TileIntegrationTable table;

	public ContainerIntegrationTable(InventoryPlayer playerInventory, TileIntegrationTable table) {
		super(table.getSizeInventory());
		this.table = table;

		for (int i = 0; i < 9; i++) {
			addSlot(new SlotValidated(table, i, SLOT_X[i], SLOT_Y[i]));
		}

		addSlot(new SlotOutput(table, 9, 138, 49));
		addSlot(new SlotUntouchable(table.clientOutputInv, 0, 101, 36));

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 109 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 167));
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
