/**
 * Copyright (c) Krapht, 2011
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import buildcraft.core.utils.INBTTagable;
import java.util.LinkedList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

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
				onInventoryChanged();
				return result;
			}
			ItemStack stack = _contents[slotId];
			_contents[slotId] = null;
			onInventoryChanged();
			return stack;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if (slotId >= _contents.length) {
			return;
		}
		this._contents[slotId] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return _name;
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
	}

	@Override
	public void onInventoryChanged() {
		for (TileEntity handler : _listener) {
			handler.onInventoryChanged();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		readFromNBT(data, "items");
	}

	public void readFromNBT(NBTTagCompound data, String tag) {
		NBTTagList nbttaglist = data.getTagList(tag);

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			NBTTagCompound slot = (NBTTagCompound) nbttaglist.tagAt(j);
			int index;
			if (slot.hasKey("index")) {
				index = slot.getInteger("index");
			} else {
				index = slot.getByte("Slot");
			}
			if (index >= 0 && index < _contents.length) {
				_contents[index] = ItemStack.loadItemStackFromNBT(slot);
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
		this._contents[slotId] = null;
		return stackToTake;
	}

	public ItemStack[] getItemStacks() {
		return _contents;
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
}
