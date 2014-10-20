/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PerditionCalculator;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.RFBattery;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipePowerWood extends Pipe<PipeTransportPower> implements IPowerReceptor, IPipeTransportPowerHook, IEnergyHandler {

	public final boolean[] powerSources = new boolean[6];

	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	protected RFBattery[] batteries = new RFBattery[6];

	private boolean full;
	private int requestedEnergy, sources;
	private PowerHandler powerHandler;

	public PipePowerWood(Item item) {
		super(new PipeTransportPower(), item);

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			batteries[o.ordinal()] = new RFBattery(320 * 50, 320, 0);
		}

		powerHandler = new PowerHandler(this, Type.PIPE);
		powerHandler.configure(0, 500, 1, 1500);
		powerHandler.setPerdition(new PerditionCalculator(PerditionCalculator.MIN_POWERLOSS));
		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction != ForgeDirection.UNKNOWN && powerSources[direction.ordinal()]) {
			return solidIconIndex;
		} else {
			return standardIconIndex;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		sources = 0;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			boolean oldPowerSource = powerSources[o.ordinal()];
					
			if (!container.isPipeConnected(o)) {
				powerSources[o.ordinal()] = false;
			} else {
				TileEntity tile = container.getTile(o);
			
				if (powerSources[o.ordinal()] = isPowerSource(tile, o)) {
					sources++;
				}
			}
			
			if (oldPowerSource != powerSources[o.ordinal()]) {
				container.scheduleRenderUpdate();
			}
		}
		
		if (container.getWorldObj().isRemote) {
			// We only do the isRemote check now to get a list
			// of power sources for client-side rendering.
			return;
		}

		if (sources <= 0) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				batteries[o.ordinal()].useEnergy(0, 50, false);
			}
			requestedEnergy = 0;
			return;
		}

		int energyToRemove = requestedEnergy;

		// TODO: Have energyToRemove be precalculated
		// and used in receiveEnergy and extractEnergy.
		// That way, we can replicate BC behaviour more accurately,
		// but we still need to see how well that works with constant power.

		/* if (mjStored > 40) {
			energyToRemove = mjStored / 40 + 4;
		} else if (mjStored > 10) {
			energyToRemove = mjStored / 10;
		} else {
			energyToRemove = 1;
		} */

		//energyToRemove /= sources;

		int sourceCount = 0;

		// Extract power from RF sources.
		// While we send power to receivers and so does TE4,
		// Extra Utilities generators (as an example) depend
		// on extracting energy from them manually.
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!powerSources[o.ordinal()]) {
				continue;
			}

			TileEntity tile = container.getTile(o);

			if (tile instanceof IEnergyHandler) {
				((IEnergyHandler) tile).extractEnergy(o.getOpposite(), batteries[o.ordinal()].addEnergy(0, ((IEnergyHandler) tile).extractEnergy(o.getOpposite(), energyToRemove, true), false), false);
			}
			if (batteries[o.ordinal()].getEnergyStored() != 0) {
				sourceCount++;
			}
		}

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!powerSources[o.ordinal()]) {
				continue;
			}

			int energyToAdd = Math.min(batteries[o.ordinal()].getEnergyStored(), energyToRemove / sourceCount);
			energyToRemove -= energyToAdd;
			sourceCount--;
			batteries[o.ordinal()].setEnergy(batteries[o.ordinal()].getEnergyStored() - transport.receiveEnergy(o, energyToAdd));
		}

		requestedEnergy = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);

			NBTTagCompound batteryNBT = new NBTTagCompound();
			batteries[i].writeToNBT(batteryNBT);
			data.setTag("battery[" + i + "]", batteryNBT);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
			batteries[i].readFromNBT(data.getCompoundTag("battery[" + i + "]"));
		}
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int val) {
		return -1;
	}

	@Override
	public int requestEnergy(ForgeDirection from, int amount) {
		if (container.getTile(from) instanceof IPipeTile) {
			requestedEnergy += amount;
			return amount;
		} else {
			return 0;
		}
	}

	public boolean isPowerSource(TileEntity tile, ForgeDirection from) {
		if (!transport.inputOpen(from)) {
			return false;
		} else if (tile instanceof IPowerEmitter && ((IPowerEmitter) tile).canEmitPowerFrom(from.getOpposite())) {
			return true;
		} else {
			return tile instanceof IEnergyHandler && ((IEnergyHandler) tile).canConnectEnergy(from.getOpposite());
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		//battery.addEnergy(0, (int) Math.round(this.powerHandler.getEnergyStored() * 10), true);
		//this.powerHandler.setEnergy(0.0);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		if (powerSources[from.ordinal()]) {
			return batteries[from.ordinal()].receiveEnergy(maxReceive, simulate);
		} else {
			return 0;
		}
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(from == ForgeDirection.UNKNOWN){
			return 0;
		}
		return batteries[from.ordinal()].getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(from == ForgeDirection.UNKNOWN){
			return 0;
		}
		return batteries[from.ordinal()].getMaxEnergyStored();
	}
}
