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
import net.minecraft.inventory.Slot;

import buildcraft.core.gui.BuildCraftContainer;

public class ContainerRedstoneBoard extends BuildCraftContainer {

	public ContainerRedstoneBoard(EntityPlayer player, int x, int y, int z) {
		super(player.inventory.getSizeInventory());

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 8 + sx * 18, 140 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new Slot(player.inventory, sx, 8 + sx * 18, 198));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
