/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;

public class ContainerEngineRoot extends BuildCraftContainer
{

	public ContainerEngineRoot(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
		super (tileEngine);
		
		engine = tileEngine;

		if (tileEngine.engine instanceof EngineStone) {
			addSlot(new Slot(tileEngine, 0, 80, 41));
		} else {
			addSlot(new Slot(tileEngine, 0, 52, 41));
		}

		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < 9; k++) {
				addSlot(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18,
						84 + i * 18));
			}

		}

		for (int j = 0; j < 9; j++) {
			addSlot(new Slot(inventoryplayer, j, 8 + j * 18, 142));
		}
	}

    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        return engine.isUseableByPlayer(entityplayer);
    }

    protected TileEngine engine;

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return engine.isUseableByPlayer(entityplayer);
	}
}
