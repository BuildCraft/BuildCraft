/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import buildcraft.api.core.NetworkData;
import buildcraft.api.transport.IPipeTile;

public class TriggerParameterItemStack implements ITriggerParameter {

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
	public void clicked(IPipeTile pipe, IStatement stmt, ItemStack stack, int mouseButton) {
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
		// Legacy code to prevent existing gates from losing their contents
		int itemID = compound.getInteger("itemID");
		if (itemID != 0) {
			stack = new ItemStack((Item) Item.itemRegistry.getObject(itemID), 1, compound.getInteger("itemDMG"));
			return;
		}

		stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
	}

	@Override
	public String getDescription() {
		return stack.getDisplayName();
	}
}
