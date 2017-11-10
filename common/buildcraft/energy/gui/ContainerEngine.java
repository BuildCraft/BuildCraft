/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.engines.TileEngineWithInventory;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.energy.TileEngineStone;

public class ContainerEngine extends BuildCraftContainer {

	protected TileEngineWithInventory engine;

	public ContainerEngine(InventoryPlayer inventoryplayer, TileEngineWithInventory tileEngine) {
		super(tileEngine.getSizeInventory());

		engine = tileEngine;

		if (tileEngine instanceof TileEngineStone) {
			addSlotToContainer(new Slot(tileEngine, 0, 80, 41));
		} else {
			addSlotToContainer(new Slot(tileEngine, 0, 52, 41));
		}

		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 9; k++) {
				addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
			}

		}

		for (int j = 0; j < 9; j++) {
			addSlotToContainer(new Slot(inventoryplayer, j, 8 + j * 18, 142));
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (Object crafter : crafters) {
			engine.sendGUINetworkData(this, (ICrafting) crafter);
		}
	}

	@Override
	public void updateProgressBar(int i, int j) {
		engine.getGUINetworkData(i, j);
	}

	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return engine.isUseableByPlayer(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return engine.isUseableByPlayer(entityplayer);
	}
}
