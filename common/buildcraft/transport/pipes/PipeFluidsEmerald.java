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
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IClientState;
import buildcraft.core.utils.FluidUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;

public class PipeFluidsEmerald extends PipeFluidsWood implements IClientState {
	private SimpleInventory filters = new SimpleInventory(1, "Filters", 1);

	public PipeFluidsEmerald(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipeFluidsEmerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();

        transport.initFromPipe(getClass());
		transport.travelDelay = 4;
	}

	public IInventory getFilters() {
		return filters;
	}

	@Override
	public int extractFluid(IFluidHandler fluidHandler, ForgeDirection side) {
		Fluid targetFluid = FluidUtils.getFluidFromItemStack(filters.getStackInSlot(0));
		if (targetFluid == null) {
			return super.extractFluid(fluidHandler, side);
		}

		int flowRate = transport.flowRate;
		FluidStack toExtract = new FluidStack(targetFluid, liquidToExtract > flowRate ? flowRate : liquidToExtract);
		FluidStack extracted = fluidHandler.drain(side.getOpposite(), toExtract, false);

		if (extracted != null) {
			toExtract.amount = transport.fill(side, extracted, true);
			fluidHandler.drain(side.getOpposite(), toExtract, true);
		}
		return toExtract.amount;
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
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_EMERALD_FLUID, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
		}

		return true;
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
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		filters.writeToNBT(nbt);
	}
}
