/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IAction;
import buildcraft.api.inventory.ISelectiveInventory;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.EnumColor;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.pipes.PipeItemsEmerald.ButtonState;
import buildcraft.transport.triggers.ActionExtractionPreset;

public class PipeItemsLogemerald extends PipeItemsWood {

	private final SimpleInventory filters = new SimpleInventory(4, "Filters", 1);

	private BitSet activeFlags = new BitSet(4);
	private int filterCount = filters.getSizeInventory();
	private int currentFilter = 0;

	public PipeItemsLogemerald(int itemID) {
		super(itemID);

		standardIconIndex = PipeIconProvider.TYPE.PipeItemsLogemerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllLogemerald_Solid.ordinal();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length) {
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe) {
				return false;
			}
		}

		if (super.blockActivated(entityplayer)) {
			return true;
		}

		if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_LOGEMERALD_ITEM, container.worldObj, container.xCoord, container.yCoord, container.zCoord);
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

		if (activeFlags.isEmpty()){
			return null;
		}
		
		incrementFilter();

		if (filters.getStackInSlot(currentFilter % filterCount) == null || !activeFlags.get(currentFilter % filterCount)){
			return null;
		}
		
		/* ISELECTIVEINVENTORY */
		if (inventory instanceof ISelectiveInventory) {
			ItemStack[] stacks = ((ISelectiveInventory) inventory).extractItem(new ItemStack[] { getCurrentFilter() }, false, doRemove, from, (int) powerHandler.getEnergyStored());
			if (doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;

			/* ISPECIALINVENTORY */
		} else if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(false, from, (int) powerHandler.getEnergyStored());
			if (stacks != null) {
				for (ItemStack stack : stacks) {
					if (stack == null)
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
					stacks = ((ISpecialInventory) inventory).extractItem(true, from, (int) powerHandler.getEnergyStored());
					for (ItemStack stack : stacks) {
						if (stack != null) {
							powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
						}
					}
				}
			}
			return stacks;

		} else {

			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);
			ItemStack result = checkExtractGeneric(inv, doRemove, from);
						
			if (result != null) {
				return new ItemStack[] { result };
			}
		}

		return null;
	}

	@Override
	public ItemStack checkExtractGeneric(net.minecraft.inventory.ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		for (int i : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.stackSize > 0) {
				ItemStack filter = getCurrentFilter();
				if (filter == null) {
					return null;
				}
				if (!filter.isItemEqual(stack)) {
					continue;
				}
				if (!inventory.canExtractItem(i, stack, from.ordinal())) {
					continue;
				}
				if (doRemove) {
					return inventory.decrStackSize(i, (int) powerHandler.useEnergy(1, stack.stackSize, true));
				} else {
					return stack;
				}
			}
		}

		return null;
	}

	public IInventory getFilters() {
		return filters;
	}
	
	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);
		
		activeFlags.clear();

		for (Entry<IAction, Boolean> action : actions.entrySet()) {
			if (action.getKey() instanceof ActionExtractionPreset && action.getValue() != null && action.getValue()) {
				setActivePreset(((ActionExtractionPreset) action.getKey()).color);
			}
		}
	}

	private void setActivePreset(EnumColor color) {
		switch (color){
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
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> result = super.getActions();
		
		result.add(BuildCraftTransport.actionExtractionPresetRed);
		result.add(BuildCraftTransport.actionExtractionPresetBlue);
		result.add(BuildCraftTransport.actionExtractionPresetGreen);
		result.add(BuildCraftTransport.actionExtractionPresetYellow);

		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		filters.readFromNBT(nbt);
		currentFilter = nbt.getInteger("currentFilter");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
		nbt.setInteger("currentFilter", currentFilter);
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
		return filters.getStackInSlot(currentFilter % filters.getSizeInventory());
	}
}
