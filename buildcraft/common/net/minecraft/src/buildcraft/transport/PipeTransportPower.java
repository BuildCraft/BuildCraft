package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeTransportPower extends PipeTransport {
	
	public int [] powerQuery = new int [6];
	public int [] nextPowerQuery = new int [6];
	public long currentDate;
	
	public double [] internalPower = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	public double [] internalNextPower = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	public double [] displayPower = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

	public double powerResitance = 0.01;
	
	public PipeTransportPower () {
		for (int i = 0; i < 6; ++i) {
			powerQuery [i] = 0;
		}
	}
	
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		return tile instanceof TileGenericPipe
    	    || tile instanceof IMachine;
	}
	
	@Override
	public void updateEntity () {
		step ();
		
		TileEntity tiles [] = new TileEntity [6];
		
		// Extract the nearby connected tiles
		
		for (int i = 0; i < 6; ++i) {
			Position p = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);
			
			p.moveForwards(1.0);

			if (Utils.checkPipesConnections(worldObj, (int) p.x, (int) p.y,
					(int) p.z, xCoord, yCoord, zCoord)) {
				
				tiles [i] = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
						(int) p.z);
			}
		}
		
		// Send the power to nearby pipes who requested it
		
		displayPower = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		
		for (int i = 0; i < 6; ++i) {
			if (internalPower [i] > 0) {
				double div = 0;
				
				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0) {
						if (tiles[j] instanceof TileGenericPipe
								|| tiles[j] instanceof IPowerReceptor) {
							div += powerQuery[j];
						}
					}
				}
				
				double totalWatt = internalPower [i];
												
				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery [j] > 0) {
						double watts = (totalWatt / div * powerQuery [j]);
						
						if (tiles [j] instanceof TileGenericPipe) {							
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles [j];
		
							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
							
							nearbyTransport.receiveEnergy(
									Orientations.values()[j].reverse(), watts);
							
							displayPower [j] += watts;
							displayPower [i] += watts;
							
							internalPower [i] -= watts;
						} else if (tiles [j] instanceof IPowerReceptor) {
							IPowerReceptor pow = (IPowerReceptor) tiles [j];

							pow.getPowerProvider().receiveEnergy((int) watts);

							displayPower [j] += watts;
							displayPower [i] += watts;

							internalPower [i] -= watts;
						}
					}
				}
			}					
		}
		
		// Compute the tiles requesting energy that are not pipes
		
		for (int i = 0; i < 6; ++i) {
			if (tiles[i] instanceof IPowerReceptor
					&& !(tiles[i] instanceof TileGenericPipe)) {
				PowerProvider provider = ((IPowerReceptor) tiles[i])
						.getPowerProvider();

				if (provider != null && provider.maxEnergyReceived > 0) {
					requestEnergy(Orientations.values()[i],
							provider.maxEnergyReceived);
				}
			}
		}
		
		// Sum the amount of energy requested on each side
		
		int transferQuery [] = {0, 0, 0, 0, 0, 0};
		
		for (int i = 0; i < 6; ++i) {
			transferQuery [i] = 0;
			
			for (int j = 0; j < 6; ++j) {
				if (j != i) {
					transferQuery [i] += powerQuery [j];
				}
			}
		}
		
		// Transfer the requested energy to nearby pipes
		
		for (int i = 0; i < 6; ++i) {
			if (transferQuery[i] != 0) {
				if (tiles[i] != null) {
					TileEntity entity = tiles[i];

					if (entity instanceof TileGenericPipe) {
						TileGenericPipe nearbyTile = (TileGenericPipe) entity;

						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

						nearbyTransport.requestEnergy(
								Orientations.values()[i].reverse(),
								transferQuery[i]);
					}
				}
			}
		}					
	}
	
	public void step () {
		if (currentDate != worldObj.getWorldTime()) {
			currentDate = worldObj.getWorldTime();
			
			powerQuery = nextPowerQuery;
			nextPowerQuery = new int [] {0, 0, 0, 0, 0, 0};
			
			internalPower = internalNextPower;
			internalNextPower = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		}
	}

	public void receiveEnergy(Orientations from, double val) {
		step ();
		internalNextPower [from.ordinal()] += val * (1 - powerResitance);		
	}
	
	public void requestEnergy(Orientations from, int i) {
		step ();
		nextPowerQuery [from.ordinal()] += i;		
	}
	
	@Override
	public void initialize () {
		currentDate = worldObj.getWorldTime();
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);		
		
		for (int i = 0; i < 6; ++i) {
			powerQuery [i] = nbttagcompound.getInteger("powerQuery[" + i + "]");
			nextPowerQuery [i] = nbttagcompound.getInteger("nextPowerQuery[" + i + "]");
			internalPower [i] = nbttagcompound.getDouble("internalPower[" + i + "]");
			internalNextPower [i] = nbttagcompound.getDouble("internalNextPower[" + i + "]");
		}
			
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		for (int i = 0; i < 6; ++i) {
			nbttagcompound.setInteger("powerQuery[" + i + "]", powerQuery [i]);
			nbttagcompound.setInteger("nextPowerQuery[" + i + "]", nextPowerQuery [i]);
			nbttagcompound.setDouble("internalPower[" + i + "]", internalPower [i]);
			nbttagcompound.setDouble("internalNextPower[" + i + "]", internalNextPower [i]);
		}
	}

}
