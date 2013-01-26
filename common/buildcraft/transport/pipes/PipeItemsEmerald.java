/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.inventory.ISelectiveInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.GuiIds;
import buildcraft.core.network.IClientState;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeTransportItems;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class PipeItemsEmerald extends PipeItemsWood implements ISpecialInventory, IClientState {

	private SimpleInventory filters = new SimpleInventory(9, "items", 1);
	private int currentFilter = 0;

	protected PipeItemsEmerald(int itemID, PipeTransportItems transport) {
		super(transport, new PipeLogicEmerald(), itemID);

		baseTexture = 6 * 16 + 13;
		plainTexture = baseTexture + 1;
	}

	public PipeItemsEmerald(int itemID) {
		this(itemID, new PipeTransportItems());
	}

	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length) {
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe) {
				return false;
			}
		}

		if (super.blockActivated(worldObj, x, y, z, entityplayer)) {
			return true;
		}

		if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_EMERALD_ITEM, worldObj, x, y, z);
		}

		return true;
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	@Override
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {

		// ISELECTIVEINVENTORY
		if (inventory instanceof ISelectiveInventory) {
			ItemStack[] stacks = ((ISelectiveInventory) inventory).extractItem(new ItemStack[]{getCurrentFilter()}, false, doRemove, from, (int) getPowerProvider().getEnergyStored());
			if (doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						getPowerProvider().useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
				incrementFilter();
			}
			return stacks;
		}


		// ISPECIALINVENTORY
		if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(false, from, (int) getPowerProvider().getEnergyStored());
			if (stacks != null) {
				for (ItemStack stack : stacks) {
					if(stack == null)
						continue;
					
					boolean matches = false;
					for (int i = 0; i < filters.getSizeInventory(); i++) {
						ItemStack filter = filters.getStackInSlot(i);
						if (filter != null && filter.isItemEqual(stack)) {
							matches = true;
							break;
						}
					}
					if (!matches) {
						return null;
					}
				}
				if (doRemove) {
					stacks = ((ISpecialInventory) inventory).extractItem(true, from, (int) getPowerProvider().getEnergyStored());
					for (ItemStack stack : stacks) {
						if (stack != null) {
							getPowerProvider().useEnergy(stack.stackSize, stack.stackSize, true);
						}
					}
				}
			}
			return stacks;
		}

		if (inventory instanceof ISidedInventory) {
			ISidedInventory sidedInv = (ISidedInventory) inventory;

			int first = sidedInv.getStartInventorySide(from);
			int last = first + sidedInv.getSizeInventorySide(from) - 1;

			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, first, last);

			if (result != null) {
				return new ItemStack[]{result};
			}
		} else {
			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, 0, inv.getSizeInventory() - 1);

			if (result != null) {
				return new ItemStack[]{result};
			}
		}

		return null;
	}

	private void incrementFilter() {
		currentFilter++;
		int count = 0;
		while (filters.getStackInSlot(currentFilter % filters.getSizeInventory()) == null && count < filters.getSizeInventory()) {
			currentFilter++;
			count++;
		}
	}

	private ItemStack getCurrentFilter() {
		ItemStack filter = filters.getStackInSlot(currentFilter % filters.getSizeInventory());
		if (filter == null) {
			incrementFilter();
		}
		return filters.getStackInSlot(currentFilter % filters.getSizeInventory());
	}

	@Override
	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from, int start, int stop) {
		for (int i = start; i <= stop; ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.stackSize > 0) {
				ItemStack filter = getCurrentFilter();
				if (filter == null) {
					return null;
				}
				if (!filter.isItemEqual(stack)) {
					continue;
				}
				if (doRemove) {
					incrementFilter();
					return inventory.decrStackSize(i, (int) getPowerProvider().useEnergy(1, stack.stackSize, true));
				} else {
					return stack;
				}
			}
		}

		return null;
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

	// ICLIENTSTATE
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		NBTBase.writeNamedTag(nbt, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		NBTBase nbt = NBTBase.readNamedTag(data);
		if (nbt instanceof NBTTagCompound) {
			readFromNBT((NBTTagCompound) nbt);
		}
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
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == container;
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
