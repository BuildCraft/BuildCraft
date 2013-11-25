/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.network.IClientState;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsDiamond extends Pipe<PipeTransportItems> implements IClientState {

	private SimpleInventory filters = new SimpleInventory(54, "Filters", 1);

	public PipeItemsDiamond(int itemID) {
		super(new PipeTransportItems(), itemID);
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
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length)
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe)
				return false;

		if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.worldObj, container.xCoord, container.yCoord, container.zCoord);
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

				if (filter != null)
					foundFilter = true;

				if (StackHelper.instance().isMatchingItem(filter, event.item.getItemStack(), true, false))
					filteredOrientations.add(dir);
			}
			if (!foundFilter)
				defaultOrientations.add(dir);
		}
		event.destinations.clear();
		if (!filteredOrientations.isEmpty())
			event.destinations.addAll(filteredOrientations);
		else
			event.destinations.addAll(defaultOrientations);
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
}
