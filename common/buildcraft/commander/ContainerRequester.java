/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.core.gui.BuildCraftContainer;

public class ContainerRequester extends BuildCraftContainer {

	public GuiRequester gui;

	private TileRequester requester;

	public ContainerRequester(IInventory playerInventory, TileRequester iRequester) {
		super(iRequester.getSizeInventory());

		requester = iRequester;

		for (int x = 0; x < 4; ++x) {
			for (int y = 0; y < 5; ++y) {
				addSlotToContainer(new Slot(iRequester, x * 5 + y, 117 + x * 18, 7 + y * 18));
			}
		}

		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 19 + k1 * 18, 101 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 19 + i1 * 18, 159));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}
