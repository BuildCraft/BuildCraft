/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerScienceBook extends BuildCraftContainer {

	private EntityPlayer player;

	public ContainerScienceBook(EntityPlayer iPlayer) {
		super(iPlayer.inventory.getSizeInventory());

		player = iPlayer;

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 19 + sx * 18, 101 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new Slot(player.inventory, sx, 19 + sx * 18, 159));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
