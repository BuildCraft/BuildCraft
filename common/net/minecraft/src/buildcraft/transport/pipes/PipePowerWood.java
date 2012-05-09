/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.PipeTransportPower;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private PowerProvider powerProvider;

	private int baseTexture = 7 * 16 + 6;
	private int plainTexture = 1 * 16 + 15;
	private int nextTexture = baseTexture;

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 1000, 1, 1000);
		powerProvider.configurePowerPerdition(1, 100);
	}

	@Override
	public int getMainBlockTexture() {
		return nextTexture;
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		provider = powerProvider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		for (Orientations o : Orientations.dirs())
			if (Utils.checkPipesConnections(container,
					container.getTile(o))) {
				TileEntity tile = container.getTile(o);

				if (tile instanceof TileGenericPipe) {
					PipeTransportPower pow = (PipeTransportPower) ((TileGenericPipe) tile).pipe.transport;

					float energyToRemove = 0;

					if (powerProvider.energyStored > 40)
						energyToRemove = powerProvider.energyStored / 40 + 4;
					else if (powerProvider.energyStored > 10)
						energyToRemove = powerProvider.energyStored / 10;
					else
						energyToRemove = 1;

					float energyUsed = powerProvider.useEnergy(1, energyToRemove, true);

					pow.receiveEnergy(o.reverse(),
							energyUsed);

					((PipeTransportPower) transport).displayPower[o.ordinal()] += energyUsed;
				}

			}
	}

	@Override
	public void prepareTextureFor(Orientations connection) {
		if (connection == Orientations.Unknown)
			nextTexture = baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == connection.ordinal())
				nextTexture = plainTexture;
			else
				nextTexture = baseTexture;
		}
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

}
