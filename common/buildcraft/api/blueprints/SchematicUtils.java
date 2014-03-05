/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SchematicUtils {

	public static void requestInventoryContents(Schematic slot, IBuilderContext context, LinkedList<ItemStack> requirements) {
		ItemStack[] stacks = getItemStacks(slot, context);

		for (ItemStack stack : stacks) {
			if (stack != null) {
				requirements.add(stack);
			}
		}
	}

	public static void initializeInventoryContents(Schematic slot, IBuilderContext context, IInventory inventory) {
		ItemStack[] stacks = new ItemStack[inventory.getSizeInventory()];

		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			stacks[i] = inventory.getStackInSlot(i);
		}

		setItemStacks(slot, context, stacks);
	}

	public static void buildInventoryContents(Schematic slot, IBuilderContext context, IInventory inventory) {
		ItemStack[] stacks = getItemStacks(slot, context);

		for (int i = 0; i < stacks.length; ++i) {
			inventory.setInventorySlotContents(i, stacks[i]);
		}
	}

	public static ItemStack[] getItemStacks(Schematic slot, IBuilderContext context) {
		NBTTagList list = (NBTTagList) slot.cpt.getTag("inv");

		if (list == null) {
			return new ItemStack[0];
		}

		ItemStack stacks[] = new ItemStack[list.tagCount()];

		for (int i = 0; i < list.tagCount(); ++i) {
			//ItemStack stack = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));

			//if (stack != null && stack.itemID != 0 && stack.stackSize > 0) {
			//	stacks[i] = context.mapItemStack(stack);
			//}
		}

		return stacks;
	}

	public static void setItemStacks(Schematic slot, IBuilderContext context, ItemStack[] stacks) {
		NBTTagList nbttaglist = new NBTTagList();

		for (ItemStack stack : stacks) {
			NBTTagCompound cpt = new NBTTagCompound();
			nbttaglist.appendTag(cpt);
			if (stack != null && stack.stackSize != 0) {
				stack.writeToNBT(cpt);
				//context.storeId(stack.itemID);
			}
		}

		slot.cpt.setTag("inv", nbttaglist);
	}

}
