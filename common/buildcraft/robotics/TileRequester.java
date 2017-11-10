/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

public class TileRequester extends TileBuildCraft implements IInventory, IRequestProvider, ICommandReceiver {
	public static final int NB_ITEMS = 20;

	private SimpleInventory inv = new SimpleInventory(NB_ITEMS, "items", 64);
	private SimpleInventory requests = new SimpleInventory(NB_ITEMS, "requests", 64);

	public TileRequester() {

	}

	public void setRequest(final int index, final ItemStack stack) {
		if (worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setRequest", new CommandWriter() {
				public void write(ByteBuf data) {
					data.writeByte(index);
					NetworkUtils.writeStack(data, stack);
				}
			}));
		} else {
			requests.setInventorySlotContents(index, stack);
		}
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "setRequest".equals(command)) {
			setRequest(stream.readUnsignedByte(), NetworkUtils.readStack(stream));
		}
	}

	public ItemStack getRequestTemplate(int index) {
		return requests.getStackInSlot(index);
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inv.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return inv.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return inv.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		inv.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public String getInventoryName() {
		return inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return inv.isUseableByPlayer(entityPlayer);
	}

	@Override
	public void openInventory() {
		inv.openInventory();
	}

	@Override
	public void closeInventory() {
		inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if (requests.getStackInSlot(i) == null) {
			return false;
		} else if (!StackHelper.isMatchingItemOrList(requests.getStackInSlot(i), itemStack)) {
			return false;
		} else {
			return inv.isItemValidForSlot(i, itemStack);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagCompound invNBT = new NBTTagCompound();
		inv.writeToNBT(invNBT);
		nbt.setTag("inv", invNBT);

		NBTTagCompound reqNBT = new NBTTagCompound();
		requests.writeToNBT(reqNBT);
		nbt.setTag("req", reqNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		inv.readFromNBT(nbt.getCompoundTag("inv"));
		requests.readFromNBT(nbt.getCompoundTag("req"));
	}

	public boolean isFulfilled(int i) {
		if (requests.getStackInSlot(i) == null) {
			return true;
		} else if (inv.getStackInSlot(i) == null) {
			return false;
		} else {
			return StackHelper.isMatchingItemOrList(requests.getStackInSlot(i), inv.getStackInSlot(i))
					&& inv.getStackInSlot(i).stackSize >= requests.getStackInSlot(i).stackSize;
		}
	}

	@Override
	public int getRequestsCount() {
		return NB_ITEMS;
	}

	@Override
	public ItemStack getRequest(int i) {
		if (requests.getStackInSlot(i) == null) {
			return null;
		} else if (isFulfilled(i)) {
			return null;
		} else {
			ItemStack request = requests.getStackInSlot(i).copy();

			ItemStack existingStack = inv.getStackInSlot(i);
			if (existingStack == null) {
				return request;
			}

			if (!StackHelper.isMatchingItemOrList(request, existingStack)) {
				return null;
			}

			request.stackSize -= existingStack.stackSize;
			if (request.stackSize <= 0) {
				return null;
			}

			return request;
		}
	}

	@Override
	public ItemStack offerItem(int i, ItemStack stack) {
		ItemStack existingStack = inv.getStackInSlot(i);

		if (requests.getStackInSlot(i) == null) {
			return stack;
		} else if (existingStack == null) {
			if (!StackHelper.isMatchingItemOrList(stack, requests.getStackInSlot(i))) {
				return stack;
			}

			int maxQty = requests.getStackInSlot(i).stackSize;

			if (stack.stackSize <= maxQty) {
				inv.setInventorySlotContents(i, stack);

				return null;
			} else {
				ItemStack newStack = stack.copy();
				newStack.stackSize = maxQty;
				stack.stackSize -= maxQty;

				inv.setInventorySlotContents(i, newStack);

				return stack;
			}
		} else if (!StackHelper.isMatchingItemOrList(stack, existingStack)) {
			return stack;
		} else if (StackHelper.isMatchingItemOrList(stack, requests.getStackInSlot(i))) {
			int maxQty = requests.getStackInSlot(i).stackSize;

			if (existingStack.stackSize + stack.stackSize <= maxQty) {
				existingStack.stackSize += stack.stackSize;
				return null;
			} else {
				stack.stackSize -= maxQty - existingStack.stackSize;
				existingStack.stackSize = maxQty;
				return stack;
			}
		} else {
			return stack;
		}
	}
}
