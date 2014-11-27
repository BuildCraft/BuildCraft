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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.RFBattery;
import buildcraft.core.network.IClientState;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;

public class PipeFluidsWood extends Pipe<PipeTransportFluids> implements IEnergyHandler, IClientState {
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
			if (!PipeManager.canExtractFluids(pipe, tile.getWorldObj (), tile.xCoord, tile.yCoord, tile.zCoord)) {
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
			EnumFacing side = EnumFacing.getOrientation(meta);
			TileEntity tile = container.getTile(side);

			if (tile instanceof IFluidHandler) {
				liquidToExtract -= extractFluid((IFluidHandler) tile, side);
			}
		}

		if (battery.useEnergy(10, 10, false) > 0) {
			if (meta > 5) {
				return;
			}

			TileEntity tile = container.getTile(EnumFacing
					.getOrientation(meta));

			if (tile instanceof IFluidHandler) {
				if (!PipeManager.canExtractFluids(this, tile.getWorld(),
						tile.xCoord, tile.yCoord, tile.zCoord)) {
					return;
				}

				if (liquidToExtract <= FluidContainerRegistry.BUCKET_VOLUME) {
					liquidToExtract += FluidContainerRegistry.BUCKET_VOLUME;
				}
			}
		}
	}

	public int extractFluid(IFluidHandler fluidHandler, EnumFacing side) {
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
	public int getIconIndex(EnumFacing direction) {
		if (direction == EnumFacing.UNKNOWN) {
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
	public boolean outputOpen(EnumFacing to) {
		int meta = container.getBlockMetadata();
		return super.outputOpen(to) && meta != to.ordinal();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
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
