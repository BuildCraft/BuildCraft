/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;


public interface ILiquidContainer {
	
	public int fill (Orientations from, int quantity, int id, boolean doFill);
	
	public int empty (int quantityMax, boolean doEmpty);
	
	public int getLiquidQuantity ();
	
	public int getCapacity ();
	
	public int getLiquidId ();
}
