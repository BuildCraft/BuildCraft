/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.silicon;

import java.util.LinkedList;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.EntityEnergyLaser;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;

public class TileLaser extends TileEntity implements IPowerReceptor {

	private EntityEnergyLaser laser = null;
	
	private SafeTimeTracker laserTickTracker = new SafeTimeTracker();
	private SafeTimeTracker searchTracker = new SafeTimeTracker();
	
	private TileAssemblyTable assemblyTable;
	 
	private PowerProvider powerProvider;
	
	private int nextLaserUpdate = 10;
	private int nextLaserSearch = 200;
	
	public TileLaser () {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);
	}
		
	@Override
	public void updateEntity () {
		if (powerProvider.energyStored == 0) {
			if (laser != null) {
				deleteLaser();
			}
			
			return;
		}
		
		if (searchTracker.markTimeIfDelay(worldObj, nextLaserSearch)) {
			aim ();
			nextLaserSearch = 190 + worldObj.rand.nextInt(20);
		}
		
		if (assemblyTable != null && (assemblyTable.isInvalid() || assemblyTable.currentRecipe == null)) {
			deleteLaser();
		}		
		
		if (laser != null && laserTickTracker.markTimeIfDelay(worldObj, nextLaserUpdate)) {
			setLaserPosition();
			nextLaserUpdate = 5 + worldObj.rand.nextInt(10);
		}
		
		if (assemblyTable != null) {
			float p = powerProvider.useEnergy(0, 4, true);
			laser.pushPower(p);
			assemblyTable.receiveLaserEnergy(p);
		}
	}
	
	private void deleteLaser () {
		if (laser != null) {
			laser.setDead();
			laser = null;
			assemblyTable = null;
		}
	}
	
	
	public void aim () {		
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		int minX = xCoord - 5;
		int minY = yCoord - 5;
		int minZ = zCoord - 5;
		int maxX = xCoord + 5;
		int maxY = yCoord + 5;
		int maxZ = zCoord + 5;
		
		
		switch (Orientations.values()[meta]) {
		case XNeg:
			maxX = xCoord;
			break;
		case XPos:
			minX = xCoord;
			break;
		case YNeg:
			maxY = yCoord;
			break;
		case YPos:
			minY = yCoord;
			break;
		case ZNeg:
			maxZ = zCoord;
			break;
		case ZPos:
			minZ = zCoord;
			break;
		}

		LinkedList <BlockIndex> targets = new LinkedList <BlockIndex> ();

		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {
					TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
					if (tile instanceof TileAssemblyTable) {
						TileAssemblyTable table = (TileAssemblyTable) tile;
						
						if (table.currentRecipe != null) {
							targets.add(new BlockIndex(x, y, z));
						}
					}
				}
			}	
		}
		
		if (targets.size() == 0) {
			return;
		}

		BlockIndex b = targets.get(worldObj.rand.nextInt(targets.size()));
		assemblyTable = (TileAssemblyTable) worldObj.getBlockTileEntity(b.i, b.j, b.k);
				
		if (laser == null) {
			laser = new EntityEnergyLaser(worldObj);
			setLaserPosition();
			worldObj.spawnEntityInWorld(laser);
		} else {
			setLaserPosition();	
		}
	}

	private void setLaserPosition () {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		double px = 0, py = 0, pz = 0;
		
		switch (Orientations.values()[meta]) {
		case XNeg:			
			px = -0.3;
			break;
		case XPos:
			px = 0.3;
			break;
		case YNeg:
			py = -0.3;
			break;
		case YPos:
			py = 0.3;
			break;
		case ZNeg:
			pz = -0.3;
			break;
		case ZPos:
			pz = 0.3;
			break;
		}
		
		laser.setPositions(
				xCoord + 0.5 + px,
				yCoord + 0.5 + py,
				zCoord + 0.5 + pz,
				assemblyTable.xCoord + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F,
				assemblyTable.yCoord + 9F / 16F, 
				assemblyTable.zCoord + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F);
	}
	
	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
		
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
	public int powerRequest() {
		if (powerProvider.energyStored < 200 || laser != null) {
			return 25;
		} else {
			return 0;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 25, 25, 25, 1000);
	}

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);
    }
	
	@Override
	public void invalidate () {
		super.invalidate();
		
		deleteLaser ();
	}
	
}
