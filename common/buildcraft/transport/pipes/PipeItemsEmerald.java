/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.network.IClientState;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;

public class PipeItemsEmerald extends PipeItemsWood implements IClientState, IGuiReturnHandler {

	public enum FilterMode {
		WHITE_LIST, BLACK_LIST, ROUND_ROBIN;
	}

	public class EmeraldPipeSettings {

		private FilterMode filterMode;

		public EmeraldPipeSettings() {
			filterMode = FilterMode.WHITE_LIST;
		}

		public FilterMode getFilterMode() {
			return filterMode;
		}

		public void setFilterMode(FilterMode mode) {
			filterMode = mode;
		}

		public void readFromNBT(NBTTagCompound nbt) {
			filterMode = FilterMode.values()[nbt.getByte("filterMode")];
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setByte("filterMode", (byte) filterMode.ordinal());
		}
	}

	private EmeraldPipeSettings settings = new EmeraldPipeSettings();

	private final SimpleInventory filters = new SimpleInventory(9, "Filters", 1);

	private int currentFilter = 0;

	public PipeItemsEmerald(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipeItemsEmerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null) {
			if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
				return false;
			}
		}

		if (super.blockActivated(entityplayer)) {
			return true;
		}

		if (!container.getWorldObj().isRemote) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_EMERALD_ITEM, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}

	@Override
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {
		if (inventory == null) {
			return null;
		}

		// Handle possible double chests and wrap it in the ISidedInventory interface.
		ISidedInventory sidedInventory = InventoryWrapper.getWrappedInventory(InvUtils.getInventory(inventory));

		if (settings.getFilterMode() == FilterMode.ROUND_ROBIN) {
			return checkExtractRoundRobin(sidedInventory, doRemove, from);
		}

		return checkExtractFiltered(sidedInventory, doRemove, from);
	}

	private ItemStack[] checkExtractFiltered(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		for (int k : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack stack = inventory.getStackInSlot(k);

			if (stack == null || stack.stackSize <= 0) {
				continue;
			}

			if (!inventory.canExtractItem(k, stack, from.ordinal())) {
				continue;
			}

			boolean matches = isFiltered(stack);
			boolean isBlackList = settings.getFilterMode() == FilterMode.BLACK_LIST;

			if ((isBlackList && matches) || (!isBlackList && !matches)) {
				continue;
			}

			if (doRemove) {
				double energyUsed =  mjStored > stack.stackSize ? stack.stackSize : mjStored;
				mjStored -= energyUsed;

				stack = inventory.decrStackSize(k, (int) Math.floor(energyUsed));
			}

			return new ItemStack[] {stack};
		}

		return null;
	}

	private ItemStack[] checkExtractRoundRobin(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		for (int i : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack stack = inventory.getStackInSlot(i);

			if (stack != null && stack.stackSize > 0) {
				ItemStack filter = getCurrentFilter();

				if (filter == null) {
					return null;
				}

				if (!StackHelper.isMatchingItem(filter, stack, true, false)) {
					continue;
				}

				if (!inventory.canExtractItem(i, stack, from.ordinal())) {
					continue;
				}

				if (doRemove) {
					// In Round Robin mode, extract only 1 item regardless of power level.
					stack = inventory.decrStackSize(i, 1);
					incrementFilter();
				}

				return new ItemStack[]{ stack };
			}
		}

		return null;
	}

	private boolean isFiltered(ItemStack stack) {
		for (int i = 0; i < filters.getSizeInventory(); i++) {
			ItemStack filter = filters.getStackInSlot(i);

			if (filter == null) {
				return false;
			}

			if (StackHelper.isMatchingItem(filter, stack, true, false)) {
				return true;
			}
		}

		return false;
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

	public IInventory getFilters() {
		return filters;
	}

	public EmeraldPipeSettings getSettings() {
		return settings;
	}

	@Override
	public void writeData(ByteBuf data) {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		Utils.writeNBT(data, nbt);
	}

	@Override
	public void readData(ByteBuf data) {
		NBTTagCompound nbt = Utils.readNBT(data);
		readFromNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		filters.readFromNBT(nbt);
		settings.readFromNBT(nbt);

		currentFilter = nbt.getInteger("currentFilter");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		filters.writeToNBT(nbt);
		settings.writeToNBT(nbt);

		nbt.setInteger("currentFilter", currentFilter);
	}

	@Override
	public void writeGuiData(ByteBuf data) {
		data.writeByte((byte) settings.getFilterMode().ordinal());
	}

	@Override
	public void readGuiData(ByteBuf data, EntityPlayer sender) {
		settings.setFilterMode(FilterMode.values()[data.readByte()]);
	}
}
