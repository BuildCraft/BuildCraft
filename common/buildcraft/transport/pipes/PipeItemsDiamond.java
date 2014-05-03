/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.LinkedList;

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
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.network.IClientState;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;

public class PipeItemsDiamond extends Pipe<PipeTransportItems> implements IClientState {

	private SimpleInventory filters = new SimpleInventory(54, "Filters", 1);

	public PipeItemsDiamond(Item item) {
		super(new PipeTransportItems(), item);
	}

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
	public boolean blockActivated(EntityPlayer entityplayer) {
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

	public void eventHandler(PipeEventItem.FindDest event) {
		LinkedList<ForgeDirection> filteredOrientations = new LinkedList<ForgeDirection>();
		LinkedList<ForgeDirection> defaultOrientations = new LinkedList<ForgeDirection>();

		// Filtered outputs
		for (ForgeDirection dir : event.destinations) {
			boolean foundFilter = false;

			// NB: if there's several of the same match, the probability
			// to use that filter is higher, this is why there are
			// no breaks here.
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack filter = getFilters().getStackInSlot(dir.ordinal() * 9 + slot);

				if (filter != null) {
					foundFilter = true;
				}

				if (StackHelper.isMatchingItem(filter, event.item.getItemStack(), true, false)) {
					filteredOrientations.add(dir);
				}
			}
			if (!foundFilter) {
				defaultOrientations.add(dir);
			}
		}

		event.destinations.clear();

		if (!filteredOrientations.isEmpty()) {
			event.destinations.addAll(filteredOrientations);
		} else {
			event.destinations.addAll(defaultOrientations);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		filters.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
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
}
