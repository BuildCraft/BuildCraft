/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import buildcraft.core.utils.INBTTagable;

public class SimpleInventory implements IInventory, INBTTagable {

	private final ItemStack[] _contents;
	private final String _name;
	private final int _stackLimit;
	private final LinkedList<TileEntity> _listener = new LinkedList<TileEntity>();

	public SimpleInventory(int size, String name, int stackLimit) {
		_contents = new ItemStack[size];
		_name = name;
		_stackLimit = stackLimit;
	}

	@Override
	public int getSizeInventory() {
		return _contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return _contents[slotId];
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		if (slotId < _contents.length && _contents[slotId] != null) {
			if (_contents[slotId].stackSize > count) {
				ItemStack result = _contents[slotId].splitStack(count);
				markDirty();
				return result;
			}
			ItemStack stack = _contents[slotId];
			setInventorySlotContents(slotId, null);
			return stack;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if (slotId >= _contents.length) {
			return;
		}
		_contents[slotId] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public String getInventoryName() {
		return _name;
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
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
		readFromNBT(data, "items");
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
			if (index >= 0 && index < _contents.length) {
				setInventorySlotContents(index, ItemStack.loadItemStackFromNBT(slot));
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		writeToNBT(data, "items");
	}

	public void writeToNBT(NBTTagCompound data, String tag) {
		NBTTagList slots = new NBTTagList();
		for (byte index = 0; index < _contents.length; ++index) {
			if (_contents[index] != null && _contents[index].stackSize > 0) {
				NBTTagCompound slot = new NBTTagCompound();
				slots.appendTag(slot);
				slot.setByte("Slot", index);
				_contents[index].writeToNBT(slot);
			}
		}
		data.setTag(tag, slots);
	}

	public void addListener(TileEntity listner) {
		_listener.add(listner);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		if (this._contents[slotId] == null) {
			return null;
		}

		ItemStack stackToTake = this._contents[slotId];
		setInventorySlotContents(slotId, null);
		return stackToTake;
	}

	public ItemStack[] getItemStacks() {
		return _contents;
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
		for (TileEntity handler : _listener) {
			handler.markDirty();
		}
	}
}
