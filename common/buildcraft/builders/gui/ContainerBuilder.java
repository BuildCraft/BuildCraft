/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import buildcraft.builders.TileBuilder;
import buildcraft.core.gui.BuildCraftContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerBuilder extends BuildCraftContainer {

	IInventory playerIInventory;
	TileBuilder builder;

	public ContainerBuilder(IInventory playerInventory, TileBuilder builder) {
		super(builder.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.builder = builder;

		addSlotToContainer(new Slot(builder, 0, 80, 27));

		for (int k = 0; k < 3; k++) {
			for (int j1 = 0; j1 < 9; j1++) {
				addSlotToContainer(new Slot(builder, 1 + j1 + k * 9, 8 + j1 * 18, 72 + k * 18));
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
		return builder.isUseableByPlayer(entityplayer);
	}

}
