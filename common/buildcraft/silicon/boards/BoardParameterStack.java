/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.IBoardParameterStack;

public class BoardParameterStack extends BoardParameter implements IBoardParameterStack {
	ItemStack stack;

	@Override
	public ItemStack getStack() {
		return stack;
	}

	@Override
	public void setStack(ItemStack iStack) {
		stack = iStack;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		if (stack != null) {
			NBTTagCompound stackNBT = new NBTTagCompound();
			stack.writeToNBT(stackNBT);
			nbt.setTag("stack", stackNBT);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("stack")) {
			stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
		}
	}
}
