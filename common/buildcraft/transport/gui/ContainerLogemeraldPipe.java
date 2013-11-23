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
import buildcraft.transport.pipes.PipeItemsLogemerald;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerLogemeraldPipe extends BuildCraftContainer {

	private final PipeItemsLogemerald logemeraldPipe;
	
	private final IInventory filterInv;

	public ContainerLogemeraldPipe(IInventory playerInventory, PipeItemsLogemerald pipe) {
		super(pipe.getFilters().getSizeInventory());
		
		logemeraldPipe = pipe;
		filterInv = logemeraldPipe.getFilters();

		addSlotToContainer(new SlotPhantom(filterInv, 0, 44, 21));
		addSlotToContainer(new SlotPhantom(filterInv, 1, 116, 21));
		addSlotToContainer(new SlotPhantom(filterInv, 2, 44, 49));
		addSlotToContainer(new SlotPhantom(filterInv, 3, 116, 49));
		
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 84 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return logemeraldPipe.container.isUseableByPlayer(entityplayer);
	}
}
