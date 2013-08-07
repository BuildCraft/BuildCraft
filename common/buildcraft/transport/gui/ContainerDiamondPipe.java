/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotPhantom;
import buildcraft.transport.pipes.PipeItemsDiamond;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerDiamondPipe extends BuildCraftContainer {

	private final PipeItemsDiamond pipe;
	private final IInventory playerInv;
	private final IInventory filterInv;

	public ContainerDiamondPipe(IInventory playerInventory, PipeItemsDiamond pipe) {
		super(pipe.getFilters().getSizeInventory());
		this.pipe = pipe;
		this.playerInv = playerInventory;
		this.filterInv = pipe.getFilters();

		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new SlotPhantom(filterInv, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return pipe.container.isUseableByPlayer(entityplayer);
	}
}
