package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
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

		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(50, 1, 1000, 1, 1000);
		powerProvider.configurePowerPerdition(1, 100);
	}

	@Override
	public int getBlockTexture() {
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
		
		for (int i = 0; i < 6; ++i) {
			Position p = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);

			p.moveForwards(1.0);

			if (Utils.checkPipesConnections(worldObj, xCoord, yCoord, zCoord,
					(int) p.x, (int) p.y, (int) p.z)) {
				TileEntity tile = worldObj.getBlockTileEntity((int) p.x,
						(int) p.y, (int) p.z);

				if (tile instanceof TileGenericPipe) {
					PipeTransportPower pow = (PipeTransportPower) ((TileGenericPipe) tile).pipe.transport;

					int energyToRemove = 0;
					
					if (powerProvider.energyStored > 40) {
						energyToRemove = powerProvider.energyStored / 40 + 4; 
					} else if (powerProvider.energyStored > 10) {
						energyToRemove = powerProvider.energyStored / 10;
					} else {
						energyToRemove = 1;
					}
					
					int energyUsed = powerProvider.useEnergy(1, energyToRemove, true);
					
					pow.receiveEnergy(p.orientation.reverse(),
							energyUsed);
					
					((PipeTransportPower) transport).displayPower[i] += energyUsed;
				}

			}
		}
	}

	@Override
	public void prepareTextureFor(Orientations connection) {
		if (connection == Orientations.Unknown) {
			nextTexture = baseTexture;
		} else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == connection.ordinal()) {
				nextTexture = plainTexture;
			} else {
				nextTexture = baseTexture;
			}
		}
	}
	
	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

}
