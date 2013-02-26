/**
 * Copyright (c) Krapht, 2011
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SimpleInventory implements IInventory, INBTTagable {

	private final ItemStack[] _contents;
	private final String _name;
	private final int _stackLimit;

	// private final LinkedList<ISimpleInventoryEventHandler> _listener = new
	// LinkedList<ISimpleInventoryEventHandler>();

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
		if (_contents[slotId] == null)
			return null;
		if (_contents[slotId].stackSize > count)
			return _contents[slotId].splitStack(count);
		ItemStack stack = _contents[slotId];
		_contents[slotId] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		_contents[slotId] = itemstack;

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
		// for (ISimpleInventoryEventHandler handler : _listener){
		// handler.InventoryChanged(this);
		// }
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return false;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagList nbttaglist = nbttagcompound.getTagList("items");

		for (int j = 0; j < nbttaglist.tagCount(); ++j) {
			NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
			int index = nbttagcompound2.getInteger("index");
			_contents[index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int j = 0; j < _contents.length; ++j) {
			if (_contents[j] != null && _contents[j].stackSize > 0) {
				NBTTagCompound nbttagcompound2 = new NBTTagCompound();
				nbttaglist.appendTag(nbttagcompound2);
				nbttagcompound2.setInteger("index", j);
				_contents[j].writeToNBT(nbttagcompound2);
			}
		}
		nbttagcompound.setTag("items", nbttaglist);
	}

	// public void addListener(ISimpleInventoryEventHandler listner){
	// if (!_listener.contains(listner)){
	// _listener.add(listner);
	// }
	// }
	//
	// public void removeListener(ISimpleInventoryEventHandler listner){
	// if (_listener.contains(listner)){
	// _listener.remove(listner);
	// }
	// }

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if (this._contents[i] == null)
			return null;

		ItemStack stackToTake = this._contents[i];
		this._contents[i] = null;
		return stackToTake;
	}

	public ItemStack[] getItemStacks()
	{
	    return _contents;
	}
}
