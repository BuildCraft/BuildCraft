/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private PowerHandler powerHandler;
	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	private boolean[] powerSources = new boolean[6];
	private boolean full;

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), itemID);

		powerHandler = new PowerHandler(this, Type.PIPE);
		initPowerProvider();
		((PipeTransportPower) transport).initFromPipe(getClass());
	}

	private void initPowerProvider() {
		powerHandler.configure(2, 1000, 1, 1500);
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
		if (container.worldObj.isRemote)
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

		float energyToRemove;

		if (powerHandler.getEnergyStored() > 40) {
			energyToRemove = powerHandler.getEnergyStored() / 40 + 4;
		} else if (powerHandler.getEnergyStored() > 10) {
			energyToRemove = powerHandler.getEnergyStored() / 10;
		} else {
			energyToRemove = 1;
		}
		energyToRemove /= (float) sources;

		PipeTransportPower trans = (PipeTransportPower) transport;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!powerSources[o.ordinal()])
				continue;

			float energyUsable = powerHandler.useEnergy(0, energyToRemove, false);

			float energySend = trans.receiveEnergy(o, energyUsable);
			if (energySend > 0) {
				powerHandler.useEnergy(0, energySend, true);
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
}
