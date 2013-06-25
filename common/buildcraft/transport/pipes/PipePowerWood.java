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
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private PowerProvider powerProvider;
	protected int standardIconIndex = PipeIconProvider.PipePowerWood_Standard;
	protected int solidIconIndex = PipeIconProvider.PipeAllWood_Solid;
	private boolean[] powerSources = new boolean[6];
	private boolean full;

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), new PipeLogicWood(), itemID);

		powerProvider = new PowerProvider(false);
		initPowerProvider();
		((PipeTransportPower) transport).initFromPipe(getClass());
	}

	private void initPowerProvider() {
		powerProvider.configure(2, 1000, 1, 1500);
		powerProvider.configurePowerPerdition(1, 10);
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
	public PowerProvider getPowerProvider(ForgeDirection side) {
		return powerProvider;
	}

	@Override
	public void doWork(PowerProvider workProvider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (worldObj.isRemote)
			return;

		if (powerProvider.getEnergyStored() <= 0)
			return;

		int sources = 0;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!container.isPipeConnected(o)) {
				powerSources[o.ordinal()] = false;
				continue;
			}
			if (powerProvider.isPowerSource(o)) {
				powerSources[o.ordinal()] = true;
			}
			if (powerSources[o.ordinal()]) {
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
			if (!powerSources[o.ordinal()])
				continue;

			float energyUsable = powerProvider.useEnergy(1, energyToRemove, false);

			float energySend = (float) trans.receiveEnergy(o, energyUsable);
			if (energySend > 0) {
				powerProvider.useEnergy(1, energySend, true);
			}
		}
	}

	public boolean requestsPower() {
		if (full) {
			boolean request = powerProvider.getEnergyStored() < powerProvider.getMaxEnergyStored() / 2;
			if (request) {
				full = false;
			}
			return request;
		}
		full = powerProvider.getEnergyStored() >= powerProvider.getMaxEnergyStored() - 10;
		return !full;
	}


	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		powerProvider.writeToNBT(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		powerProvider.readFromNBT(data);
		initPowerProvider();
	}
}
