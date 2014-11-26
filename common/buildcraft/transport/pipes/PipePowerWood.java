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
import net.minecraft.util.EnumFacing;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.RFBattery;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipePowerWood extends Pipe<PipeTransportPower> implements IPipeTransportPowerHook, IEnergyHandler {

	public final boolean[] powerSources = new boolean[6];

	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	protected RFBattery battery;

	private boolean full;
	private int requestedEnergy, sources;

	public PipePowerWood(Item item) {
		super(new PipeTransportPower(), item);

		battery = new RFBattery(320 * 50, 320, 0);
		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		if (direction != EnumFacing.UNKNOWN && powerSources[direction.ordinal()]) {
			return solidIconIndex;
		} else {
			return standardIconIndex;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		sources = 0;

		for (EnumFacing o : EnumFacing.values()) {
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
		
		if (container.getWorld().isRemote) {
			// We only do the isRemote check now to get a list
			// of power sources for client-side rendering.
			return;
		}

		if (sources <= 0) {
			battery.useEnergy(0, 50, false);
			requestedEnergy = 0;
			return;
		}

		if (sources == 0) {
			return;
		}

		int energyToRemove = Math.min(battery.getEnergyStored(), requestedEnergy);

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

		energyToRemove /= sources;

		if (battery.getEnergyStored() > 0) {
			for (EnumFacing o : EnumFacing.values()) {
				if (!powerSources[o.ordinal()]) {
					continue;
				}

				battery.setEnergy(battery.getEnergyStored() - transport.receiveEnergy(o, energyToRemove));
			}
		}

		requestedEnergy = 0;
	}

	public boolean requestsPower() {
		if (full) {
			boolean request = battery.getEnergyStored() < battery.getMaxEnergyStored() / 2;

			if (request) {
				full = false;
			}

			return request;
		}

		full = battery.getEnergyStored() >= battery.getMaxEnergyStored() - 100;

		return !full;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		NBTTagCompound batteryNBT = new NBTTagCompound();
		battery.writeToNBT(batteryNBT);
		data.setTag("battery", batteryNBT);

		for (int i = 0; i < EnumFacing.values().length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		battery.readFromNBT(data.getCompoundTag("battery"));

		for (int i = 0; i < EnumFacing.values().length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
	}

	@Override
	public int receiveEnergy(EnumFacing from, int val) {
		return -1;
	}

	@Override
	public int requestEnergy(EnumFacing from, int amount) {
		if (container.getTile(from) instanceof IPipeTile) {
			requestedEnergy += amount;
			return amount;
		} else {
			return 0;
		}
	}

	public boolean isPowerSource(TileEntity tile, EnumFacing from) {
		if (!transport.inputOpen(from)) {
			return false;
		} else {
			return tile instanceof IEnergyConnection && ((IEnergyConnection) tile).canConnectEnergy(from.getOpposite());
		}
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		if (from.ordinal() < 6 && powerSources[from.ordinal()]) {
			return battery.receiveEnergy(maxReceive, simulate);
		} else {
			return 0;
		}
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
}
