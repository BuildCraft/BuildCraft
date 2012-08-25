/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy.gui;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.energy.EngineStone;
import buildcraft.energy.TileEngine;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerEngine extends BuildCraftContainer {

	protected TileEngine engine;

	public ContainerEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(tileEngine.getSizeInventory());

		engine = tileEngine;

		if (tileEngine.engine instanceof EngineStone) {
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
	public void updateCraftingResults() {
		super.updateCraftingResults();

		for (int i = 0; i < crafters.size(); i++)
			engine.engine.sendGUINetworkData(this, (ICrafting) crafters.get(i));
	}

	@Override
	public void updateProgressBar(int i, int j) {
		engine.engine.getGUINetworkData(i, j);
	}

	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return engine.isUseableByPlayer(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return engine.isUseableByPlayer(entityplayer);
	}
}
