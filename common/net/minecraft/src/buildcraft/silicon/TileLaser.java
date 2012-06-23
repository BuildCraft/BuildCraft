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
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.EntityEnergyLaser;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;

public class TileLaser extends TileBuildCraft implements IPowerReceptor {

	private EntityEnergyLaser laser = null;
	
	private final SafeTimeTracker laserTickTracker = new SafeTimeTracker();
	private final SafeTimeTracker searchTracker = new SafeTimeTracker();
	private final SafeTimeTracker networkTracker = new SafeTimeTracker();
	
	private TileAssemblyTable assemblyTable;

	@TileNetworkData
	public PowerProvider powerProvider;

	private int nextNetworkUpdate = 3;
	private int nextLaserUpdate = 10;
	private int nextLaserSearch = 10;

	public TileLaser() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);
	}

	@Override
	public void updateEntity() {
		
		if (powerProvider.energyStored == 0) {
			removeLaser();
			return;
		}
		
		if (!isValidTable()) {
			
			if (canFindTable()) {
				findTable();
			}
		}
		
		if (!isValidTable()) {
			removeLaser();
			return;
		}
		
		if (laser == null) {
			createLaser();
		}
	
		if (laser != null && canUpdateLaser()) {
			updateLaser();
		}

		float p = powerProvider.useEnergy(0, 4, true);
		assemblyTable.receiveLaserEnergy(p);
		
		if (laser != null) {
			laser.pushPower(p);
		}
		
		sendNetworkUpdate();
	}
	
	protected boolean canFindTable() {
		return searchTracker.markTimeIfDelay(worldObj, nextLaserSearch);
	}
	
	protected boolean canUpdateLaser() {
		return laserTickTracker.markTimeIfDelay(worldObj, nextLaserUpdate);
	}
	
	protected boolean isValidTable() {
		
		if (assemblyTable == null || assemblyTable.isInvalid() || assemblyTable.currentRecipe == null) {
			return false;
		}
		
		return true;
	}
	
	protected void findTable() {
		
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

		LinkedList<BlockIndex> targets = new LinkedList<BlockIndex>();

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
	}
	
	protected void createLaser() {
		
		if (!APIProxy.isClient(worldObj))
			return;
		
		laser = new EntityEnergyLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord, zCoord));
		worldObj.spawnEntityInWorld(laser);
	}
	
	protected void updateLaser() {
		
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
		
		Position head = new Position(xCoord + 0.5 + px, yCoord + 0.5 + py, zCoord + 0.5 + pz);
		Position tail = new Position(
				assemblyTable.xCoord + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F,
				assemblyTable.yCoord + 9F / 16F, 
				assemblyTable.zCoord + 0.475 + (worldObj.rand.nextFloat() - 0.5) / 5F);
		
		laser.setPositions(head, tail);
		laser.show();
	}
	
	protected void removeLaser() {
		
		if (laser != null) {
			laser.setDead();
			laser = null;
		}
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
	public void doWork() {}

	@Override
	public int powerRequest() {
		if (powerProvider.energyStored < 200 || laser != null) {
			return 25;
		} else {
			return 0;
		}
	}
	
	@Override
	public void sendNetworkUpdate() {
		
		if (networkTracker.markTimeIfDelay(worldObj, nextNetworkUpdate)) {
			super.sendNetworkUpdate();
		}
	};

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
	public void invalidate() {
		super.invalidate();
		removeLaser();
	}

}
