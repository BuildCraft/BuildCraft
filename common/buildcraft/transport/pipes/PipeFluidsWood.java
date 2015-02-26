/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.RFBattery;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;

public class PipeFluidsWood extends Pipe<PipeTransportFluids> implements IEnergyHandler, ISerializable {
	public int liquidToExtract;

	protected int standardIconIndex = PipeIconProvider.TYPE.PipeFluidsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	private long lastMining = 0;
	private boolean lastPower = false;
	private RFBattery battery = new RFBattery(2500, 1000, 0);

	private PipeLogicWood logic = new PipeLogicWood(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof IPipeTile) {
				return false;
			}
			if (!(tile instanceof IFluidHandler)) {
				return false;
			}

			return true;
		}
	    };

	public PipeFluidsWood(Item item) {
	        super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		logic.onNeighborBlockChange(blockId);
		super.onNeighborBlockChange(blockId);
	}

	@Override
	public void initialize() {
		logic.initialize();
		super.initialize();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		int meta = container.getBlockMetadata();
		
		if (liquidToExtract > 0 && meta < 6) {
			ForgeDirection side = ForgeDirection.getOrientation(meta);
			TileEntity tile = container.getTile(side);

			if (tile instanceof IFluidHandler) {
				liquidToExtract -= extractFluid((IFluidHandler) tile, side);
			}
		}

		if (battery.useEnergy(10, 10, false) > 0) {
			if (meta > 5) {
				return;
			}

			TileEntity tile = container.getTile(ForgeDirection
					.getOrientation(meta));

			if (tile instanceof IFluidHandler) {
				if (liquidToExtract <= FluidContainerRegistry.BUCKET_VOLUME) {
					liquidToExtract += FluidContainerRegistry.BUCKET_VOLUME;
				}
			}
		}
	}

	public int extractFluid(IFluidHandler fluidHandler, ForgeDirection side) {
		int flowRate = transport.flowRate;
		FluidStack extracted = fluidHandler.drain(side.getOpposite(), liquidToExtract > flowRate ? flowRate : liquidToExtract, false);

		int inserted = 0;

		if (extracted != null) {
			inserted = transport.fill(side, extracted, true);

			fluidHandler.drain(side.getOpposite(), inserted, true);
		}
		return inserted;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN) {
			return standardIconIndex;
		} else {
			int metadata = container.getBlockMetadata();

			if (metadata == direction.ordinal()) {
				return solidIconIndex;
			} else {
				return standardIconIndex;
			}
		}
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		int meta = container.getBlockMetadata();
		return super.outputOpen(to) && meta != to.ordinal();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return battery.getMaxEnergyStored();
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(liquidToExtract);
	}

	@Override
	public void readData(ByteBuf data) {
		liquidToExtract = data.readInt();
	}
}
