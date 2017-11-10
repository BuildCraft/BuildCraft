/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.builders.TileBuilder;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.widgets.ScrollbarWidget;

public class ContainerBuilder extends BuildCraftContainer {
	protected ScrollbarWidget scrollbarWidget;
	protected IInventory playerIInventory;
	protected TileBuilder builder;

	public ContainerBuilder(IInventory playerInventory, TileBuilder builder) {
		super(builder.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.builder = builder;

		this.scrollbarWidget = new ScrollbarWidget(172, 17, 18, 0, 108);
		this.scrollbarWidget.hidden = true;
		this.addWidget(scrollbarWidget);

		addSlotToContainer(new Slot(builder, 0, 80, 27));

		for (int k = 0; k < 3; k++) {
			for (int j1 = 0; j1 < 9; j1++) {
				addSlotToContainer(new Slot(builder, 1 + j1 + k * 9, 8 + j1 * 18, 72 + k * 18));
			}
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 198));
		}

		if (!builder.getWorldObj().isRemote && playerInventory instanceof InventoryPlayer) {
			// Refresh the requirements list for the player opening the GUI,
			// in case he does not have it.
			builder.updateRequirementsOnGuiOpen(((InventoryPlayer) playerInventory).player);
			builder.addGuiWatcher(((InventoryPlayer) playerInventory).player);
		}
	}

	public TileBuilder getBuilder() {
		return builder;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		builder.removeGuiWatcher(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return builder.isUseableByPlayer(entityplayer);
	}

}
