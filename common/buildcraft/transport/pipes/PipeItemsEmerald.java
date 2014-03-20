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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.core.GuiIds;
import buildcraft.core.gui.buttons.IButtonTextureSet;
import buildcraft.core.gui.buttons.IMultiButtonState;
import buildcraft.core.gui.buttons.MultiButtonController;
import buildcraft.core.gui.buttons.StandardButtonTextureSets;
import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.gui.tooltips.ToolTipLine;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IClientState;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;

public class PipeItemsEmerald extends PipeItemsWood implements IClientState, IGuiReturnHandler {

	public static enum ButtonState implements IMultiButtonState {

		BLOCKING("gui.pipes.emerald.blocking"), NONBLOCKING("gui.pipes.emerald.nonblocking");
		private final String label;

		private ButtonState(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return StringUtils.localize(this.label);
		}

		@Override
		public IButtonTextureSet getTextureSet() {
			return StandardButtonTextureSets.SMALL_BUTTON;
		}

		@Override
		public ToolTip getToolTip() {
			return this.tip;
		}
		private final ToolTip tip = new ToolTip(500) {
			@Override
			public void refresh() {
				clear();
				tip.add(new ToolTipLine(StringUtils.localize(label + ".tip")));
			}
		};
	}
	private final MultiButtonController stateController = MultiButtonController.getController(ButtonState.BLOCKING.ordinal(), ButtonState.values());
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

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	@Override
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {
		IInventory inv = InvUtils.getInventory(inventory);
		ItemStack result = checkExtractGeneric(inv, doRemove, from);

		// check through every filter once if non-blocking
		if (doRemove
				&& stateController.getButtonState() == ButtonState.NONBLOCKING
				&& result == null) {
			int count = 1;
			while (result == null && count < filters.getSizeInventory()) {
				incrementFilter();
				result = checkExtractGeneric(inv, doRemove, from);
				count++;
			}
		}

		if (result != null) {
			return new ItemStack[] { result };
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
					incrementFilter();
					double energyUsed =  mjStored > stack.stackSize ? stack.stackSize : mjStored;
					mjStored -= energyUsed;

					return inventory.decrStackSize(i, (int) energyUsed);
				} else {
					return stack;
				}
			}
		}

		return null;
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		filters.readFromNBT(nbt);
		currentFilter = nbt.getInteger("currentFilter");

		stateController.readFromNBT(nbt, "state");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
		nbt.setInteger("currentFilter", currentFilter);

		stateController.writeToNBT(nbt, "state");
	}

	// ICLIENTSTATE
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

	public IInventory getFilters() {
		return filters;
	}

	public MultiButtonController getStateController() {
		return stateController;
	}

	@Override
	public void writeGuiData(ByteBuf data) {
		data.writeByte(stateController.getCurrentState());
	}

	@Override
	public void readGuiData(ByteBuf data, EntityPlayer sender) {
		stateController.setCurrentState(data.readByte());
	}
}
