/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.ISerializable;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.utils.FluidUtils;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;

public class PipeFluidsEmerald extends PipeFluidsWood implements ISerializable {
	private SimpleInventory filters = new SimpleInventory(1, "Filters", 1);

	public PipeFluidsEmerald(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipeFluidsEmerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();

		transport.initFromPipe(getClass());
	}

	public IInventory getFilters() {
		return filters;
	}

	@Override
	public int extractFluid(IFluidHandler fluidHandler, ForgeDirection side) {
		FluidStack targetFluidStack = FluidUtils.getFluidStackFromItemStack(filters.getStackInSlot(0));
		if (targetFluidStack == null) {
			return super.extractFluid(fluidHandler, side);
		}

		int flowRate = transport.getFlowRate();
		FluidStack toExtract = new FluidStack(targetFluidStack, liquidToExtract > flowRate ? flowRate : liquidToExtract);
		FluidStack extracted = fluidHandler.drain(side.getOpposite(), toExtract, false);

		if (extracted != null) {
			toExtract.amount = transport.fill(side, extracted, true);
			if (toExtract.amount > 0) {
				fluidHandler.drain(side.getOpposite(), toExtract, true);
			}
		}
		return toExtract.amount;
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
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_EMERALD_FLUID, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
	}

	@Override
	public void writeData(ByteBuf data) {
		NBTTagCompound nbt = new NBTTagCompound();
		filters.writeToNBT(nbt);
		NetworkUtils.writeNBT(data, nbt);
	}

	@Override
	public void readData(ByteBuf data) {
		NBTTagCompound nbt = NetworkUtils.readNBT(data);
		filters.readFromNBT(nbt);
	}

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
}
