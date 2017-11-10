/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;

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
import buildcraft.api.core.EnumColor;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.network.IGuiReturnHandler;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.statements.ActionExtractionPreset;

public class PipeItemsEmzuli extends PipeItemsWood implements IGuiReturnHandler {

	public final byte[] slotColors = new byte[4];
	private final SimpleInventory filters = new SimpleInventory(4, "Filters", 1);
	private final BitSet activeFlags = new BitSet(4);
	private final int filterCount = filters.getSizeInventory();
	private int currentFilter = 0;

	public PipeItemsEmzuli(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipeItemsEmzuli_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmzuli_Solid.ordinal();
	}

	@Override
	public void onPostTick() {
		// TODO: This has a side-effect of skipping an extract every now and
		// then if an item cannot be found, but at least it's 100% reliable.

		super.onPostTick();
		incrementFilter();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer, ForgeDirection side) {
		if (entityplayer.getCurrentEquippedItem() != null) {
			if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
				return false;
			}
		}

		if (super.blockActivated(entityplayer, side)) {
			return true;
		}

		if (!container.getWorldObj().isRemote) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_LOGEMERALD_ITEM, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}

	@Override
	protected TravelingItem makeItem(double x, double y, double z, ItemStack stack) {
		TravelingItem item = super.makeItem(x, y, z, stack);
		int color = slotColors[currentFilter % filterCount];
		if (color > 0) {
			item.color = EnumColor.fromId(color - 1);
		}
		return item;
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	@Override
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {
		if (activeFlags.isEmpty()) {
			return null;
		}

		if (filters.getStackInSlot(currentFilter % filterCount) == null || !activeFlags.get(currentFilter % filterCount)) {
			return null;
		}

		IInventory inv = InvUtils.getInventory(inventory);
		ItemStack result = checkExtractGeneric(inv, doRemove, from);

		if (result != null) {
			return new ItemStack[]{result};
		}

		return null;
	}

	@Override
	public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		if (inventory == null) {
			return null;
		}

		ItemStack filter = getCurrentFilter();
		if (filter == null) {
			return null;
		}

		for (int k : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from.ordinal())) {
				if (!StackHelper.isMatchingItemOrList(slot, filter)) {
					continue;
				}

				if (doRemove) {
					int maxStackSize = slot.stackSize;
					int stackSize = Math.min(maxStackSize, battery.getEnergyStored() / 10);
					int energyUsed = (int) (stackSize * 10 * speedMultiplier);
					battery.useEnergy(energyUsed, energyUsed, false);

					return inventory.decrStackSize(k, stackSize);
				} else {
					return slot;
				}
			}
		}

		return null;
	}

	public IInventory getFilters() {
		return filters;
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		super.actionsActivated(actions);

		activeFlags.clear();

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionExtractionPreset) {
				setActivePreset(((ActionExtractionPreset) action.statement).color);
			}
		}
	}

	private void setActivePreset(EnumColor color) {
		switch (color) {
			case RED:
				activeFlags.set(0);
				break;
			case BLUE:
				activeFlags.set(1);
				break;
			case GREEN:
				activeFlags.set(2);
				break;
			case YELLOW:
				activeFlags.set(3);
				break;
			default:
				break;
		}
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> result = super.getActions();

		result.add(BuildCraftTransport.actionExtractionPresetRed);
		result.add(BuildCraftTransport.actionExtractionPresetBlue);
		result.add(BuildCraftTransport.actionExtractionPresetGreen);
		result.add(BuildCraftTransport.actionExtractionPresetYellow);

		return result;
	}

	@Override
	public void writeGuiData(ByteBuf paramDataOutputStream) {
	}

	@Override
	public void readGuiData(ByteBuf data, EntityPlayer paramEntityPlayer) {
		byte slot = data.readByte();
		slotColors[slot] = data.readByte();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		filters.readFromNBT(nbt);
		currentFilter = nbt.getInteger("currentFilter");
		for (int slot = 0; slot < slotColors.length; slot++) {
			slotColors[slot] = nbt.getByte("slotColors[" + slot + "]");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
		nbt.setInteger("currentFilter", currentFilter);
		for (int slot = 0; slot < slotColors.length; slot++) {
			nbt.setByte("slotColors[" + slot + "]", slotColors[slot]);
		}
	}

	private void incrementFilter() {
		int count = 0;
		currentFilter++;

		while (!(filters.getStackInSlot(currentFilter % filterCount) != null && activeFlags.get(currentFilter % filterCount)) && count < filterCount) {
			currentFilter++;
			count++;
		}
	}

	private ItemStack getCurrentFilter() {
		return filters.getStackInSlot(currentFilter % filterCount);
	}
}
