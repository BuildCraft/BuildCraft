/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.INBTStoreable;

public class SimpleInventory implements IInventory, INBTStoreable {

	private final ItemStack[] contents;
	private final String name;
	private final int stackLimit;
	private final LinkedList<TileEntity> listener = new LinkedList<TileEntity>();

	public SimpleInventory(int size, String invName, int invStackLimit) {
		contents = new ItemStack[size];
		name = invName;
		stackLimit = invStackLimit;
	}

	@Override
	public int getSizeInventory() {
		return contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return contents[slotId];
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		if (slotId < contents.length && contents[slotId] != null) {
			if (contents[slotId].stackSize > count) {
				ItemStack result = contents[slotId].splitStack(count);
				markDirty();
				return result;
			}
			if (contents[slotId].stackSize < count) {
				return null;
			}
			ItemStack stack = contents[slotId];
			setInventorySlotContents(slotId, null);
			return stack;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if (slotId >= contents.length) {
			return;
		}
		contents[slotId] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public String getInventoryName() {
		return name;
	}

	@Override
	public int getInventoryStackLimit() {
		return stackLimit;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if (data.hasKey("items")) {
			// this is to support legacy item load, the new format should be
			// "Items"
			readFromNBT(data, "items");
		} else {
			readFromNBT(data, "Items");
		}
	}

	public void readFromNBT(NBTTagCompound data, String tag) {
		NBTTagList nbttaglist = data.getTagList(tag, Constants.NBT.TAG_COMPOUND);

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			NBTTagCompound slot = nbttaglist.getCompoundTagAt(j);
			int index;
			if (slot.hasKey("index")) {
				index = slot.getInteger("index");
			} else {
				index = slot.getByte("Slot");
			}
			if (index >= 0 && index < contents.length) {
				setInventorySlotContents(index, ItemStack.loadItemStackFromNBT(slot));
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		writeToNBT(data, "Items");
	}

	public void writeToNBT(NBTTagCompound data, String tag) {
		NBTTagList slots = new NBTTagList();
		for (byte index = 0; index < contents.length; ++index) {
			if (contents[index] != null && contents[index].stackSize > 0) {
				NBTTagCompound slot = new NBTTagCompound();
				slots.appendTag(slot);
				slot.setByte("Slot", index);
				contents[index].writeToNBT(slot);
			}
		}
		data.setTag(tag, slots);
	}

	public void addListener(TileEntity listner) {
		listener.add(listner);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		if (this.contents[slotId] == null) {
			return null;
		}

		ItemStack stackToTake = this.contents[slotId];
		setInventorySlotContents(slotId, null);
		return stackToTake;
	}

	public ItemStack[] getItemStacks() {
		return contents;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public void markDirty() {
		for (TileEntity handler : listener) {
			handler.markDirty();
		}
	}
}
