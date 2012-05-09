/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

public class LiquidSlot {
	private int liquidId;
	private int liquidQty;
	private int capacity;
	
	public LiquidSlot (int liquidId, int liquidQty, int capacity) {
		this.liquidId = liquidId;
		this.liquidQty = liquidQty;
		this.capacity = capacity;
	}
	
	public int getLiquidId () {
		return liquidId;
	}
	
	public int getLiquidQty () {
		return liquidQty;
	}
	
	public int getCapacity () {
		return capacity;
	}
}
