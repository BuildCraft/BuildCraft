package net.minecraft.src.buildcraft.core.utils;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;

public interface ISecuredInventory extends ISpecialInventory {
	
	public String getOwnerName();
	public boolean addItem (ItemStack stack, boolean doAdd, Orientations from, String owner);
	public ItemStack extractItem(boolean doRemove, Orientations from, String owner);

}
