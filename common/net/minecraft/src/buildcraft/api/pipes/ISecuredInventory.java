package net.minecraft.src.buildcraft.api.pipes;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;

public interface ISecuredInventory extends ISpecialInventory {
	
	boolean allowsInteraction(String username);
	public boolean addItem (ItemStack stack, boolean doAdd, Orientations from, String owner);
	public ItemStack extractItem(boolean doRemove, Orientations from, String owner);

}
