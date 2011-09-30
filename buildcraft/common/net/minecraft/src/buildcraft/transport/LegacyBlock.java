/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class LegacyBlock extends BlockContainer {
	
	public int newPipeId;
	
	public LegacyBlock (int itemId, int newPipeId) {
		super (itemId, Material.glass);
		this.newPipeId = newPipeId;
	}

	@Override
	public TileEntity getBlockEntity() {		
		return new LegacyTile();
	}

}
