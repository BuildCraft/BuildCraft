/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotPhantom;

public class ContainerEmeraldPipe extends BuildCraftContainer {

	IInventory playerIInventory;
	IInventory filterIInventory;

	public ContainerEmeraldPipe(IInventory playerInventory, IInventory filterInventory) {
		super(filterInventory.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.filterIInventory = filterInventory;

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new SlotPhantom(filterInventory, i, 8 + i * 18, 18));
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 50 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 108));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return filterIInventory.isUseableByPlayer(entityplayer);
	}
}
