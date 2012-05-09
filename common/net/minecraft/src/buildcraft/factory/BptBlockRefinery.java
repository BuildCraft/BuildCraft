/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.IBptContext;
import net.minecraft.src.buildcraft.api.Orientations;

public class BptBlockRefinery extends BptBlock {

	public BptBlockRefinery(int blockId) {
		super(blockId);
	}
	

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		slot.meta = Orientations.values()[slot.meta].rotateLeft().ordinal();
	}
	
	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {
		TileRefinery refinery = (TileRefinery) context.world().getBlockTileEntity(x, y, z);
		
		slot.cpt.setInteger ("filter0", refinery.getFilter(0));
		slot.cpt.setInteger ("filter1", refinery.getFilter(1));
	}
	
	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);
		
		TileRefinery refinery = (TileRefinery) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		refinery.setFilter(0, slot.cpt.getInteger("filter0"));
		refinery.setFilter(1, slot.cpt.getInteger("filter1"));
	}


}
