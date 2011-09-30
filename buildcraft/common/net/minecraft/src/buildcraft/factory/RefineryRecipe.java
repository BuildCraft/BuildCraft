/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

public class RefineryRecipe {

	public final int sourceId1;
	public final int sourceId2;
	public final int sourceQty1;
	public final int sourceQty2;
	public final int energy;
	public final int resultId;
	public final int resultQty;
	public final int delay;
	
	public RefineryRecipe(int sourceId1, int sourceQty1, int sourceId2,
			int sourceQty2, int energy, int resultId, int resultQty, int delay) {
		this.sourceId1 = sourceId1;
		this.sourceId2 = sourceId2;
		this.sourceQty1 = sourceQty1;
		this.sourceQty2 = sourceQty2;
		this.energy = energy;
		this.resultId = resultId;
		this.resultQty = resultQty;
		this.delay = delay;
	}
	
	
}
