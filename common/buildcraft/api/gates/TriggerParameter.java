/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.gates;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class TriggerParameter implements ITriggerParameter {

	protected ItemStack stack;

	/* (non-Javadoc)
	 * @see net.minecraft.src.buildcraft.api.gates.ITriggerParameter#getItemStack()
	 */
	@Override
	public ItemStack getItemStack() {
		return stack;
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.src.buildcraft.api.gates.ITriggerParameter#set(net.minecraft.src.ItemStack)
	 */
	@Override
	public void set(ItemStack stack) {
		if (stack != null) {
			this.stack = stack.copy();
			this.stack.stackSize = 1;
		}
	}

	/* (non-Javadoc)
	 * @see net.minecraft.src.buildcraft.api.gates.ITriggerParameter#writeToNBT(net.minecraft.src.NBTTagCompound)
	 */
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (stack != null) {
			compound.setInteger("itemID", stack.itemID);
			compound.setInteger("itemDMG", stack.getItemDamage());
		}
	}

	/* (non-Javadoc)
	 * @see net.minecraft.src.buildcraft.api.gates.ITriggerParameter#readFromNBT(net.minecraft.src.NBTTagCompound)
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		int itemID = compound.getInteger("itemID");

		if (itemID != 0) {
			stack = new ItemStack(itemID, 1, compound.getInteger("itemDMG"));
		}
	}

	/* (non-Javadoc)
	 * @see net.minecraft.src.buildcraft.api.gates.ITriggerParameter#getItem()
	 */
	@Override
	public ItemStack getItem() {
		return stack;
	}

}
