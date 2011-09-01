package net.minecraft.src.buildcraft.core;

import net.minecraft.src.buildcraft.api.Orientations;

public interface ILiquidContainer {
	public int fill (Orientations from, int quantity, int id);
	
	public int empty (int quantityMax, boolean doEmpty);
	
	public int getLiquidQuantity ();
	
	public int getCapacity ();
	
	public int getLiquidId ();
}
