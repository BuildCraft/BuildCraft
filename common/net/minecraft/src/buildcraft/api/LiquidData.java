/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class LiquidData {

	public final int liquidId;
	public final int movingLiquidId;
	
	public final ItemStack filled;
	public final ItemStack container;

	public LiquidData (int liquidId, int movingLiquidId, Item filled) {
		this.liquidId = liquidId;		
		this.movingLiquidId = movingLiquidId;
		this.filled = new ItemStack (filled, 1);		
		this.container = new ItemStack(Item.bucketEmpty);
	}
	
	public LiquidData (int liquidId, int movingLiquidId, ItemStack filled) {
		this.liquidId = liquidId;
		this.movingLiquidId = movingLiquidId;
		this.filled = filled;		
		this.container = new ItemStack(Item.bucketEmpty);
	}
	
	public LiquidData(int liquidId, int movingLiquidId, ItemStack filled, ItemStack container) {
		this.liquidId = liquidId;		
		this.movingLiquidId = movingLiquidId;
		this.filled = filled;		
		this.container = container;
	}
	
}
