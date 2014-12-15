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
import buildcraft.api.pipes.IPipeContainer;
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
			
				if (powerSources[o.ordinal()] = transport.isPowerSource(tile, o)) {
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
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (!powerSources[o.ordinal()]) {
					continue;
				}

				battery.setEnergy(battery.getEnergyStored() - transport.receiveEnergy(o, energyToRemove));
			}
		}

		requestedEnergy = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		NBTTagCompound batteryNBT = new NBTTagCompound();
		battery.writeToNBT(batteryNBT);
		data.setTag("battery", batteryNBT);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		battery.readFromNBT(data.getCompoundTag("battery"));

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int val) {
		return -1;
	}

	@Override
	public int requestEnergy(ForgeDirection from, int amount) {
		if (container.getTile(from) instanceof IPipeContainer) {
			requestedEnergy += amount;
			return amount;
		} else {
			return 0;
		}
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		if (from.ordinal() < 6 && powerSources[from.ordinal()]) {
			return battery.receiveEnergy(maxReceive, simulate);
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
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return battery.getMaxEnergyStored();
	}
}
