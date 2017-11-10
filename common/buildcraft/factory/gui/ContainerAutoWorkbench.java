/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.gui.slots.SlotUntouchable;
import buildcraft.factory.TileAutoWorkbench;

public class ContainerAutoWorkbench extends BuildCraftContainer {

	public IInventory craftResult;

	private final TileAutoWorkbench tile;
	private int lastProgress;
	private ItemStack prevOutput;

	public ContainerAutoWorkbench(InventoryPlayer inventoryplayer, TileAutoWorkbench t) {
		super(t.getSizeInventory());

		craftResult = new InventoryCraftResult();
		this.tile = t;
		addSlotToContainer(new SlotUntouchable(craftResult, 0, 93, 27));
		addSlotToContainer(new SlotOutput(tile, TileAutoWorkbench.SLOT_RESULT, 124, 35));
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				addSlotToContainer(new SlotWorkbench(tile, 10 + x + y * 3, 30 + x * 18, 17 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(tile, x, 8 + x * 18, 84));
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(inventoryplayer, x + y * 9 + 9, 8 + x * 18, 115 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(inventoryplayer, x, 8 + x * 18, 173));
		}

		onCraftMatrixChanged(tile);
	}

	@Override
	public void addCraftingToCrafters(ICrafting icrafting) {
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, tile.progress);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (Object crafter : crafters) {
			ICrafting icrafting = (ICrafting) crafter;

			if (lastProgress != tile.progress) {
				icrafting.sendProgressBarUpdate(this, 0, tile.progress);
			}
		}

		ItemStack output = craftResult.getStackInSlot(0);
		if (output != prevOutput) {
			prevOutput = output;
			onCraftMatrixChanged(tile.craftMatrix);
		}

		lastProgress = tile.progress;
	}

	@Override
	public void updateProgressBar(int id, int data) {
		switch (id) {
			case 0:
				tile.progress = data;
				break;
		}
	}

	@Override
	public final void onCraftMatrixChanged(IInventory inv) {
		super.onCraftMatrixChanged(inv);
		tile.craftMatrix.rebuildCache();
		ItemStack output = tile.craftMatrix.getRecipeOutput();
		craftResult.setInventorySlotContents(0, output);
	}

	@Override
	public ItemStack slotClick(int i, int j, int modifier, EntityPlayer entityplayer) {
		ItemStack stack = super.slotClick(i, j, modifier, entityplayer);
		onCraftMatrixChanged(tile.craftMatrix);
		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tile.isUseableByPlayer(entityplayer);
	}
}
