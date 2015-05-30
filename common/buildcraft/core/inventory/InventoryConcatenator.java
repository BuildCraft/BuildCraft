/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

/**
 * Allows you to deal with multiple inventories through a single interface.
 */
public final class InventoryConcatenator implements IInventory {

	private final List<Integer> slotMap = new ArrayList<Integer>();
	private final List<IInventory> invMap = new ArrayList<IInventory>();

	private InventoryConcatenator() {
	}

	public static InventoryConcatenator make() {
		return new InventoryConcatenator();
	}

	public InventoryConcatenator add(IInventory inv) {
		for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
			slotMap.add(slot);
			invMap.add(inv);
		}
		return this;
	}

	@Override
	public int getSizeInventory() {
		return slotMap.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return invMap.get(slot).getStackInSlot(slotMap.get(slot));
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return invMap.get(slot).decrStackSize(slotMap.get(slot), amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return invMap.get(slot).getStackInSlotOnClosing(slotMap.get(slot));
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		invMap.get(slot).setInventorySlotContents(slotMap.get(slot), stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer playerIn) {

	}

	@Override
	public void closeInventory(EntityPlayer playerIn) {

	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return invMap.get(slot).isItemValidForSlot(slotMap.get(slot), stack);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public void markDirty() {
	}

	@Override
	public String getCommandSenderName() {
		return "";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return null;
	}
}
