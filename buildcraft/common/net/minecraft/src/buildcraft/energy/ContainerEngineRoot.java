/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerEngineRoot extends Container
{

	public ContainerEngineRoot(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
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
        return engine.canInteractWith(entityplayer);
    }

    protected TileEngine engine;

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
