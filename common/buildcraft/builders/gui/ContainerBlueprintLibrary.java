/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import java.util.LinkedList;

import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.BptBase;
import buildcraft.core.gui.BuildCraftContainer;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

public class ContainerBlueprintLibrary extends BuildCraftContainer {

	public LinkedList<BptBase> contents = new LinkedList<BptBase>();

	protected IInventory playerInventory;
	protected TileBlueprintLibrary library;

	public ContainerBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(library.getSizeInventory());
		this.playerInventory = player.inventory;
		this.library = library;

		// if (player.username.equals(library.owner)) {
		addSlotToContainer(new Slot(library, 0, 153, 61));
		addSlotToContainer(new Slot(library, 1, 109, 61));
		// }

		addSlotToContainer(new Slot(library, 2, 109, 79));
		addSlotToContainer(new Slot(library, 3, 153, 79));

		// Player inventory
		for (int l = 0; l < 3; l++)
			for (int k1 = 0; k1 < 9; k1++)
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));

		for (int i1 = 0; i1 < 9; i1++)
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return library.isUseableByPlayer(entityplayer);
	}
}