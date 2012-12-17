/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.transport.BlockGenericPipe;

public class PipeLogicDiamond extends PipeLogic implements ISpecialInventory {

	private SimpleInventory filters = new SimpleInventory(54, "items", 1);

	/* PIPE LOGIC */
	@Override
	public boolean doDrop() {
		return false;
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length)
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe)
				return false;

		if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.worldObj, container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		filters.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		filters.writeToNBT(nbttagcompound);
	}

	/* ISPECIALINVENTORY */
	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		return 0;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
		return new ItemStack[0];
	}

	/* IINVENTORY IMPLEMENTATION */
	@Override
	public int getSizeInventory() {
		return filters.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return filters.getStackInSlot(i);
	}

	@Override
	public String getInvName() {
		return "Filters";
	}

	@Override
	public int getInventoryStackLimit() {
		return filters.getInventoryStackLimit();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return filters.getStackInSlotOnClosing(i);
	}

	@Override
	public void onInventoryChanged() {
		filters.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack stack = filters.decrStackSize(i, j);

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {

		filters.setInventorySlotContents(i, itemstack);
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

	}

}
