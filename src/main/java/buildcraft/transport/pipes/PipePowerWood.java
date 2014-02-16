/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class PipePowerWood extends Pipe<PipeTransportPower> implements IPowerReceptor, IPipeTransportPowerHook {

	private PowerHandler powerHandler;
	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	private boolean[] powerSources = new boolean[6];
	private boolean full;

	public PipePowerWood(Item item) {
		super(new PipeTransportPower(), item);

		powerHandler = new PowerHandler(this, Type.PIPE);
		initPowerProvider();
		transport.initFromPipe(getClass());
	}

	private void initPowerProvider() {
		powerHandler.configure(2, 500, 1, 1500);
		powerHandler.configurePowerPerdition(1, 10);
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
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (container.getWorldObj().isRemote)
			return;

		if (powerHandler.getEnergyStored() <= 0)
			return;

		int sources = 0;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!container.isPipeConnected(o)) {
				powerSources[o.ordinal()] = false;
				continue;
			}
			if (powerHandler.isPowerSource(o)) {
				powerSources[o.ordinal()] = true;
			}
			if (powerSources[o.ordinal()]) {
				sources++;
			}
		}

		if (sources <= 0) {
			powerHandler.useEnergy(5, 5, true);
			return;
		}

		double energyToRemove;

		if (powerHandler.getEnergyStored() > 40) {
			energyToRemove = powerHandler.getEnergyStored() / 40 + 4;
		} else if (powerHandler.getEnergyStored() > 10) {
			energyToRemove = powerHandler.getEnergyStored() / 10;
		} else {
			energyToRemove = 1;
		}
		energyToRemove /= (float) sources;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!powerSources[o.ordinal()])
				continue;

			float energyUsable = (float) powerHandler.useEnergy(0, energyToRemove, false);

			float energySent = transport.receiveEnergy(o, energyUsable);
			if (energySent > 0) {
				powerHandler.useEnergy(0, energySent, true);
			}
		}
	}

	public boolean requestsPower() {
		if (full) {
			boolean request = powerHandler.getEnergyStored() < powerHandler.getMaxEnergyStored() / 2;
			if (request) {
				full = false;
			}
			return request;
		}
		full = powerHandler.getEnergyStored() >= powerHandler.getMaxEnergyStored() - 10;
		return !full;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		powerHandler.writeToNBT(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		powerHandler.readFromNBT(data);
		initPowerProvider();
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
	}

	@Override
	public float receiveEnergy(ForgeDirection from, float val) {
		return -1;
	}

	@Override
	public float requestEnergy(ForgeDirection from, float amount) {
		if (container.getTile(from) instanceof IPipeTile) {
			return amount;
		}
		return 0;
	}
}
