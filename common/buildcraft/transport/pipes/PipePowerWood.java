/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private IPowerProvider powerProvider;
	protected int standardIconIndex = PipeIconProvider.PipePowerWood_Standard;
	protected int solidIconIndex = PipeIconProvider.PipeAllWood_Solid;
	private int[] powerSources = new int[6];

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 2, 1000, 1, 1500);
		powerProvider.configurePowerPerdition(1, 10);
		((PipeTransportPower) transport).maxPower = 32;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (worldObj.isRemote)
			return;

		int sources = 0;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!container.isPipeConnected(o)) {
				powerSources[o.ordinal()] = 0;
				continue;
			}
			if (powerSources[o.ordinal()] > 0) {
				powerSources[o.ordinal()]--;
			}
			if (powerProvider.isPowerSource(o)) {
				powerSources[o.ordinal()] = 40;
			}
			if (powerSources[o.ordinal()] > 0) {
				sources++;
			}
		}

		if (sources <= 0)
			return;

		float energyToRemove;

		if (powerProvider.getEnergyStored() > 40) {
			energyToRemove = powerProvider.getEnergyStored() / 40 + 4;
		} else if (powerProvider.getEnergyStored() > 10) {
			energyToRemove = powerProvider.getEnergyStored() / 10;
		} else {
			energyToRemove = 1;
		}
		energyToRemove /= (float) sources;

		PipeTransportPower trans = (PipeTransportPower) transport;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (powerSources[o.ordinal()] <= 0)
				continue;

			float energyUsable = powerProvider.useEnergy(1, energyToRemove, false);

			float energySend = (float) trans.receiveEnergy(o, energyUsable);
			if (energySend > 0) {
				powerProvider.useEnergy(1, energySend, true);
			}
		}
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		return getPowerProvider().getMaxEnergyReceived();
	}
}
