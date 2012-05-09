/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class TriggerParameter {
	
	public ItemStack stack;
	
	public void set (ItemStack stack) {
		if (stack != null) {
			this.stack = stack.copy();
			this.stack.stackSize = 1;
		}
	}
	
	public void writeToNBT (NBTTagCompound compound) {
		if (stack != null) {
			compound.setInteger("itemID", stack.itemID);
			compound.setInteger("itemDMG", stack.getItemDamage());
		}
	}
	
	public void readFromNBT (NBTTagCompound compound) {
		int itemID = compound.getInteger("itemID");
		
		if (itemID != 0) {
			stack = new ItemStack(itemID, 1, compound.getInteger("itemDMG"));
		}
	}
	
	public ItemStack getItem () {
		return stack;
	}
	
}
