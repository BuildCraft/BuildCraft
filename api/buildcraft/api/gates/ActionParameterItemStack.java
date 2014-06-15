/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.api.core.NetworkData;
import buildcraft.api.transport.IPipeTile;

public class ActionParameterItemStack implements IActionParameter {

	@NetworkData
	protected ItemStack stack;

	@Override
	public ItemStack getItemStackToDraw() {
		return stack;
	}

	@Override
	public IIcon getIconToDraw() {
		return null;
	}

	@Override
	public void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack) {
		if (stack != null) {
			this.stack = stack.copy();
			this.stack.stackSize = 1;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (stack != null) {
			NBTTagCompound tagCompound = new NBTTagCompound();
			stack.writeToNBT(tagCompound);
			compound.setTag("stack", tagCompound);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ActionParameterItemStack) {
			ActionParameterItemStack param = (ActionParameterItemStack) object;

			return ItemStack.areItemStackTagsEqual(stack, param.stack);
		} else {
			return false;
		}
	}
}
