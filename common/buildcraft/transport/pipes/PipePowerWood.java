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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjAPILegacy;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipePowerWood extends Pipe<PipeTransportPower> implements IPowerReceptor, IPipeTransportPowerHook {

	public final boolean[] powerSources = new boolean[6];

	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	@MjBattery(maxCapacity = 1500, maxReceivedPerCycle = 500, minimumConsumption = 0)
	private double mjStored = 0;
	private final SafeTimeTracker sourcesTracker = new SafeTimeTracker(1);
	private boolean full;

	private MjAPILegacy powerHandler;

	public PipePowerWood(Item item) {
		super(new PipeTransportPower(), item);
		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return standardIconIndex;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		if (mjStored > 0) {
			int sources = 0;

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (!container.isPipeConnected(o)) {
					powerSources[o.ordinal()] = false;
					continue;
				}

				if (isPowerSource(o)) {
					powerSources[o.ordinal()] = true;
				}

				if (powerSources[o.ordinal()]) {
					sources++;
				}
			}

			if (sources <= 0) {
				mjStored = mjStored > 5 ? mjStored - 5 : 0;
				return;
			}

			double energyToRemove;

			if (mjStored > 40) {
				energyToRemove = mjStored / 40 + 4;
			} else if (mjStored > 10) {
				energyToRemove = mjStored / 10;
			} else {
				energyToRemove = 1;
			}
			energyToRemove /= sources;

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (!powerSources[o.ordinal()]) {
					continue;
				}

				double energyUsable = mjStored > energyToRemove ? energyToRemove : mjStored;
				double energySent = transport.receiveEnergy(o, energyUsable);

				if (energySent > 0) {
					mjStored -= energySent;
				}
			}
		}
	}

	public boolean requestsPower() {
		if (full) {
			boolean request = mjStored < 1500 / 2;

			if (request) {
				full = false;
			}

			return request;
		}

		full = mjStored >= 1500 - 10;

		return !full;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setDouble("mj", mjStored);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		mjStored = data.getDouble("mj");

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
	}

	@Override
	public double receiveEnergy(ForgeDirection from, double val) {
		return -1;
	}

	@Override
	public double requestEnergy(ForgeDirection from, double amount) {
		if (container.getTile(from) instanceof IPipeTile) {
			return amount;
		} else {
			return 0;
		}
	}

	public boolean isPowerSource(ForgeDirection from) {
		return container.getTile(from) instanceof IPowerEmitter;
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		if (powerHandler == null) {
			powerHandler = MjAPILegacy.from(container, Type.PIPE);
		}

		return powerHandler.getPowerReceiver(ForgeDirection.UNKNOWN);
	}

	@Override
	public void doWork(PowerHandler workProvider) {

	}
}
