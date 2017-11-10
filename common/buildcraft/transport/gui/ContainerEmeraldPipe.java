/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotPhantom;
import buildcraft.transport.pipes.PipeItemsEmerald;

public class ContainerEmeraldPipe extends BuildCraftContainer {

	private final PipeItemsEmerald pipe;
	private final IInventory filterInv;

	public ContainerEmeraldPipe(IInventory playerInventory, PipeItemsEmerald pipe) {
		super(pipe.getFilters().getSizeInventory());
		this.pipe = pipe;
		this.filterInv = pipe.getFilters();

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new SlotPhantom(filterInv, i, 8 + i * 18, 18));
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 79 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 137));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return pipe.container.isUseableByPlayer(entityplayer);
	}
}
