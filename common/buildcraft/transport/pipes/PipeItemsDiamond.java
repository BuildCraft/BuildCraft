/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.Iterator;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IDiamondPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.pipes.events.PipeEventPriority;

public class PipeItemsDiamond extends Pipe<PipeTransportItems> implements IDiamondPipe {
	private class SimpleFilterInventory extends SimpleInventory {
		protected int[] filterCounts = new int[6];

		public SimpleFilterInventory(int size, String invName, int invStackLimit) {
			super(size, invName, invStackLimit);
		}

		@Override
		public void markDirty() {
			super.markDirty();

			for (int i = 0; i < 6; i++) {
				filterCounts[i] = 0;
				for (int j = 0; j < 9; j++) {
					if (getStackInSlot(j + (i * 9)) != null) {
						filterCounts[i]++;
					}
				}
			}
		}
	}

	private SimpleFilterInventory filters = new SimpleFilterInventory(54, "Filters", 1);
	private long usedFilters;

	public PipeItemsDiamond(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public IInventory getFilters() {
		return filters;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		switch (direction) {
			case UNKNOWN:
				return PipeIconProvider.TYPE.PipeItemsDiamond_Center.ordinal();
			case DOWN:
				return PipeIconProvider.TYPE.PipeItemsDiamond_Down.ordinal();
			case UP:
				return PipeIconProvider.TYPE.PipeItemsDiamond_Up.ordinal();
			case NORTH:
				return PipeIconProvider.TYPE.PipeItemsDiamond_North.ordinal();
			case SOUTH:
				return PipeIconProvider.TYPE.PipeItemsDiamond_South.ordinal();
			case WEST:
				return PipeIconProvider.TYPE.PipeItemsDiamond_West.ordinal();
			case EAST:
				return PipeIconProvider.TYPE.PipeItemsDiamond_East.ordinal();
			default:
				throw new IllegalArgumentException("direction out of bounds");
		}
	}

	@Override
	public int getIconIndexForItem() {
		return PipeIconProvider.TYPE.PipeItemsDiamond_Item.ordinal();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer, ForgeDirection direction) {
		if (entityplayer.getCurrentEquippedItem() != null) {
			if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
				return false;
			}
		}

		if (!container.getWorldObj().isRemote) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}

	private boolean findDest(PipeEventItem.FindDest event) {
		for (ForgeDirection dir : event.destinations) {
			if (filters.filterCounts[dir.ordinal()] > 0) {
				for (int slot = 0; slot < 9; ++slot) {
					int v = dir.ordinal() * 9 + slot;
					if ((usedFilters & (1 << v)) != 0) {
						continue;
					}

					ItemStack filter = getFilters().getStackInSlot(v);

					if (StackHelper.isMatchingItemOrList(filter, event.item.getItemStack())) {
						usedFilters |= 1 << v;
						event.destinations.clear();
						event.destinations.add(dir);
						event.shuffle = false;
						return true;
					}
				}
			}
		}
		return false;
	}

	private void clearDest(PipeEventItem.FindDest event) {
		for (ForgeDirection dir : event.destinations) {
			if (filters.filterCounts[dir.ordinal()] > 0) {
				for (int slot = 0; slot < 9; ++slot) {
					int v = dir.ordinal() * 9 + slot;
					if ((usedFilters & (1 << v)) == 0) {
						continue;
					}

					ItemStack filter = getFilters().getStackInSlot(v);

					if (StackHelper.isMatchingItemOrList(filter, event.item.getItemStack())) {
						usedFilters ^= 1 << v;
					}
				}
			}
		}
	}

	@PipeEventPriority(priority = -4194304)
	public void eventHandler(PipeEventItem.FindDest event) {
		// We're running last and we can safely assume that nothing else
		// will change the destination.
		// This lets us skip a few logic things.

		if (findDest(event)) {
			return;
		}

		if (usedFilters != 0) {
			clearDest(event);
			if (findDest(event)) {
				return;
			}
		}

		Iterator<ForgeDirection> i = event.destinations.iterator();
		while (i.hasNext()) {
			if (filters.filterCounts[i.next().ordinal()] > 0) {
				i.remove();
			}
		}
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		filters.readFromNBT(nbt);
		if (nbt.hasKey("usedFilters")) {
			usedFilters = nbt.getLong("usedFilters");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
		nbt.setLong("usedFilters", usedFilters);
	}

	// ICLIENTSTATE
	@Override
	public void writeData(ByteBuf data) {
		NBTTagCompound nbt = new NBTTagCompound();
		filters.writeToNBT(nbt);
		nbt.setLong("usedFilters", usedFilters);
		NetworkUtils.writeNBT(data, nbt);
	}

	@Override
	public void readData(ByteBuf data) {
		NBTTagCompound nbt = NetworkUtils.readNBT(data);
		filters.readFromNBT(nbt);
		if (nbt.hasKey("usedFilters")) {
			usedFilters = nbt.getLong("usedFilters");
		}
	}
}
