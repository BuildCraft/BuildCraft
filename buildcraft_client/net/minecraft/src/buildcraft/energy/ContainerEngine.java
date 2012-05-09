/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.InventoryPlayer;

public class ContainerEngine extends ContainerEngineRoot {

    public ContainerEngine(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
		super(inventoryplayer, tileEngine);
		// TODO Auto-generated constructor stub
	}

//    @Override
//	public void updateCraftingResults() {
//		super.updateCraftingResults();
//
//		System.out.println ("UPDATE CRAFTING RESULTS");
//
////		for (int i = 0; i < crafters.size(); i++) {
////			ICrafting icrafting = (ICrafting) crafters.get(i);
////
////
////
////			if (scaledBurnTime != engine.scaledBurnTime) {
////				icrafting.updateCraftingInventoryInfo(this, 0,
////						engine.scaledBurnTime);
////			}
////		}
////
////		scaledBurnTime = engine.scaledBurnTime;
//	}

	@Override
	public void updateProgressBar(int i, int j) {
		engine.engine.getGUINetworkData (i, j);
	}
}
