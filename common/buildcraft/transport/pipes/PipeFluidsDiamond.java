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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IClientState;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IPipeTransportFluidsFilter;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportFluids.PipeSection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsDiamond extends Pipe<PipeTransportFluids> implements IPipeTransportFluidsFilter, IClientState {

	private SimpleInventory filters = new SimpleInventory(54, "Filters", 1);
	
	public PipeFluidsDiamond(int itemID) {
		super(new PipeTransportFluids(), itemID);
		transport.flowRate = 80;
		transport.travelDelay = 4;
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

//	@Override
//	public int getIconIndex(ForgeDirection direction) {
//		switch (direction) {
//			case UNKNOWN:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_Center.ordinal();
//			case DOWN:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_Down.ordinal();
//			case UP:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_Up.ordinal();
//			case NORTH:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_North.ordinal();
//			case SOUTH:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_South.ordinal();
//			case WEST:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_West.ordinal();
//			case EAST:
//				return PipeIconProvider.TYPE.PipeFluidsDiamond_East.ordinal();
//			default:
//				throw new IllegalArgumentException("direction out of bounds");
//		}
//	}
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length)
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe)
				return false;

		if (!CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_FLUID_DIAMOND, container.worldObj, container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}
	
	@Override
	public boolean filterPossibleMovements(ForgeDirection orientation, FluidStack fluid) {
		for (int slot = 0; slot < 9; ++slot) {
			ItemStack filter = getFilters().getStackInSlot(orientation.ordinal() * 9 + slot);
			if(FluidContainerRegistry.isFilledContainer(filter)) {
				if(fluid.getFluid() == FluidContainerRegistry.getFluidForFilledItem(filter).getFluid()) {
					return true;
				} else
					continue;
			}
		}
		return false;
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
