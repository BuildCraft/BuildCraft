/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private static final int MAX_OVERHEAT_TICKS = 100;

	private IPowerProvider powerProvider;

	private int baseTexture = 7 * 16 + 6;
	private int plainTexture = 1 * 16 + 15;

	private int overheatTicks;

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 2, 1000, 1, 1000);
		powerProvider.configurePowerPerdition(1, 100);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return plainTexture;
			else
				return baseTexture;
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		provider = powerProvider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		if (overheatTicks > 0) {
			return null;
		}
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (powerProvider.getEnergyStored() == powerProvider.getMaxEnergyStored()) {
			overheatTicks+=overheatTicks<MAX_OVERHEAT_TICKS ? 1 : 0;
		} else {
			overheatTicks-=overheatTicks>0 ? 1 : 0;
		}

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS)
			if (Utils.checkPipesConnections(container, container.getTile(o))) {
				TileEntity tile = container.getTile(o);

				if (tile instanceof TileGenericPipe) {
					if (((TileGenericPipe) tile).pipe == null) {
						continue; // Null pointer protection
					}

					PipeTransportPower pow = (PipeTransportPower) ((TileGenericPipe) tile).pipe.transport;

					float energyToRemove = 0;

					if (powerProvider.getEnergyStored() > 40)
						energyToRemove = powerProvider.getEnergyStored() / 40 + 4;
					else if (powerProvider.getEnergyStored() > 10)
						energyToRemove = powerProvider.getEnergyStored() / 10;
					else
						energyToRemove = 1;

					float energyUsed = powerProvider.useEnergy(1, energyToRemove, true);

					pow.receiveEnergy(o.getOpposite(), energyUsed);

					if (worldObj.isRemote) return;
					((PipeTransportPower) transport).displayPower[o.ordinal()] += energyUsed;
				}

			}
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
	}

}
