/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

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
