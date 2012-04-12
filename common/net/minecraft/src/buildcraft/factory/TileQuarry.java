/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.DefaultAreaProvider;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileQuarry extends TileMachine implements IArmListener,
		IMachine, IPowerReceptor {
	
	BlockContents nextBlockForBluePrint = null;
	boolean isDigging = false;
	
	public @TileNetworkData Box box = new Box ();
	public @TileNetworkData boolean inProcess = false;		

	public EntityMechanicalArm arm;	
	public @TileNetworkData int targetX, targetY, targetZ;
	public @TileNetworkData double headPosX, headPosY, headPosZ;
	public @TileNetworkData double speed = 0.03;
		
	boolean loadArm = false;			
	
	BluePrintBuilder bluePrintBuilder;
	
	public @TileNetworkData PowerProvider powerProvider;
	
	public static int MAX_ENERGY = 7000;
	
	public TileQuarry() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, MAX_ENERGY);
	}
	
    public void createUtilsIfNeeded () {
    	if (!box.isInitialized() && APIProxy.isClient(worldObj)) {
    		return;
    	}
    	    	
    	if (bluePrintBuilder == null) {
    		if (!box.isInitialized()) {
    			setBoundaries(loadDefaultBoundaries);
    		}
    		    
    		initializeBluePrintBuilder();
    	}    	
    	
    	nextBlockForBluePrint = bluePrintBuilder.findNextBlock(worldObj);
    	
    	if (bluePrintBuilder.done) {    	
    		box.deleteLasers();
    		
    		if (arm == null) {
    			createArm ();
    		}

    		if (loadArm) {
    			arm.joinToWorld(worldObj);
    			loadArm = false;
    			
    			if (findTarget(false)) {    				
    	    		isDigging = true;
    	    	}
    		}
    	} else {    		
    		box.createLasers(worldObj, LaserKind.Stripes);		
    		isDigging = true;
    	}
    }
	
	private boolean loadDefaultBoundaries = false;
	
	private void createArm () {
		arm = new EntityMechanicalArm(worldObj, box.xMin + Utils.pipeMaxPos,
				yCoord + bluePrintBuilder.bluePrint.sizeY - 1
						+ Utils.pipeMinPos, box.zMin + Utils.pipeMaxPos,
				bluePrintBuilder.bluePrint.sizeX - 2 + Utils.pipeMinPos * 2,
				bluePrintBuilder.bluePrint.sizeZ - 2 + Utils.pipeMinPos * 2);

		arm.listener = this;
		loadArm = true;
	}

	@Override
	public void updateEntity () {
		super.updateEntity();
		
		if (inProcess && arm != null) {
			arm.speed = 0;
			int energyToUse = 2 + powerProvider.energyStored / 1000;
			
			int energy = powerProvider
			.useEnergy(energyToUse, energyToUse, true);
						
			if (energy > 0) {
				arm.doMove(0.015 + (float) energy / 200F);
			}
		}
		
		if (arm != null) {
			headPosX = arm.headPosX;
			headPosY = arm.headPosY;
			headPosZ = arm.headPosZ;
			
			speed = arm.speed;
		}
	}
	
	public void doWork() {				
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		if (inProcess) {			
			return;
		}		
		
		if (!isDigging) {
			return;
		}
		
	    createUtilsIfNeeded();
	    
	    if (bluePrintBuilder == null) {
	    	return;
	    }	    	    
	    
    	if (bluePrintBuilder.done && nextBlockForBluePrint != null) {
    		// In this case, the Quarry has been broken. Repair it.
    		bluePrintBuilder.done = false;
    		
    		box.createLasers(worldObj, LaserKind.Stripes);
    	}
	    
		if (!bluePrintBuilder.done) {
			// configuration for building phase
			powerProvider.configure(20, 25, 25, 25, MAX_ENERGY);
			
			if (powerProvider.useEnergy(25, 25, true) != 25) {
		    	return;
		    }
			
			powerProvider.timeTracker.markTime(worldObj);
			BlockContents contents = bluePrintBuilder.findNextBlock(worldObj);
			
			int blockId = worldObj.getBlockId(contents.x, contents.y, contents.z);
						
			if (contents != null) {
				if (!API.softBlock(blockId)) {
					// Do not drop items here, too power consuming
					worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
							0);
				} else if (contents.blockId != 0) {
					worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
							contents.blockId);
				}				
			}
			
			return;
		} 	  	
		
		// configuration for digging phase
		powerProvider.configure(20, 30, 200, 50, MAX_ENERGY);
		
		if (!findTarget(true)) {
			arm.setTarget (box.xMin + arm.sizeX / 2, yCoord + 2, box.zMin + arm.sizeX / 2);
						
			isDigging = false;			
		}
		
		inProcess = true;
		
		if (APIProxy.isServerSide()) {
			sendNetworkUpdate ();
		}
	}

	public boolean findTarget (boolean doSet) {
		boolean[][] blockedColumns = new boolean[bluePrintBuilder.bluePrint.sizeX - 2][bluePrintBuilder.bluePrint.sizeZ - 2];
		
		for (int searchX = 0; searchX < bluePrintBuilder.bluePrint.sizeX - 2; ++searchX) {
			for (int searchZ = 0; searchZ < bluePrintBuilder.bluePrint.sizeZ - 2; ++searchZ) {
				blockedColumns [searchX][searchZ] = false;
			}
		}
		
		for (int searchY = yCoord + 3; searchY >= 0; --searchY) {
			int startX, endX, incX;
			
			if (searchY % 2 == 0) {
				startX = 0;
				endX = bluePrintBuilder.bluePrint.sizeX - 2;
				incX = 1;
			} else {
				startX = bluePrintBuilder.bluePrint.sizeX - 3;
				endX = -1;
				incX = -1;
			}
			
			for (int searchX = startX; searchX != endX; searchX += incX) {
				int startZ, endZ, incZ;
				
				if (searchX % 2 == searchY % 2) {
					startZ = 0;
					endZ = bluePrintBuilder.bluePrint.sizeZ - 2;
					incZ = 1;
				} else {
					startZ = bluePrintBuilder.bluePrint.sizeZ - 3;
					endZ = -1;
					incZ = -1;
				}
								
				for (int searchZ = startZ; searchZ != endZ; searchZ += incZ) {
					if (!blockedColumns [searchX][searchZ]) {
						int bx = box.xMin + searchX + 1, by = searchY, bz = box.zMin
								+ searchZ + 1;
						
						int blockId = worldObj.getBlockId(bx, by, bz);
						
						if (blockDig (blockId)) {		
							blockedColumns [searchX][searchZ] = true;						
						} else if (canDig(blockId)) {
							if (doSet) {
								arm.setTarget (bx, by + 1, bz);

								targetX = (int) arm.targetX;
								targetY = (int) arm.targetY;
								targetZ = (int) arm.targetZ;
							}
							
							return true;
						}
					}
				}
			}
		}

		return false;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
			
			loadDefaultBoundaries = false;
		} else if (nbttagcompound.hasKey("xSize")) {
			// This is a legacy save, get old data
			
			int xMin = nbttagcompound.getInteger("xMin");
			int zMin = nbttagcompound.getInteger("zMin");

			int xSize = nbttagcompound.getInteger("xSize");
			int ySize = nbttagcompound.getInteger("ySize");
			int zSize = nbttagcompound.getInteger("zSize");
			
			box.initialize(xMin, yCoord, zMin, xMin + xSize - 1, yCoord + ySize
					- 1, zMin + zSize - 1);
			
			loadDefaultBoundaries = false;
		} else {
			// This is a legacy save, compute boundaries
			
			loadDefaultBoundaries = true;
		}				
		
		targetX = nbttagcompound.getInteger("targetX");
		targetY = nbttagcompound.getInteger("targetY");
		targetZ = nbttagcompound.getInteger("targetZ");
		
		if (nbttagcompound.getBoolean("hasArm")) {
			NBTTagCompound armStore = nbttagcompound.getCompoundTag("arm");
			arm = new EntityMechanicalArm(worldObj);
			arm.readFromNBT(armStore);
			arm.listener = this;

			loadArm = true;
		}
		
		
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);		
		
		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);
		
		nbttagcompound.setInteger("targetX", targetX);
		nbttagcompound.setInteger("targetY", targetY);
		nbttagcompound.setInteger("targetZ", targetZ);
		nbttagcompound.setBoolean("hasArm", arm != null);
		
		if (arm != null) {
			NBTTagCompound armStore = new NBTTagCompound();
			nbttagcompound.setTag("arm", armStore);
			arm.writeToNBT(armStore);
		}
		
		NBTTagCompound boxTag = new NBTTagCompound();	
		box.writeToNBT(boxTag);
		nbttagcompound.setTag("box", boxTag);		
	}
	
	
	@Override
	public void positionReached(EntityMechanicalArm arm) {
		inProcess = false;
		
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		int i = (int) targetX;
		int j = (int) targetY - 1;
		int k = (int) targetZ;				
		
		int blockId = worldObj.getBlockId(i, j, k);
		
		if (canDig(blockId)) {
			powerProvider.timeTracker.markTime(worldObj);
			
			// Share this with mining well!			
			
			ItemStack stack = BuildCraftBlockUtil.getItemStackFromBlock(
					worldObj, i, j, k);

			if (stack != null) {
				boolean added = false;

				// First, try to add to a nearby chest

				StackUtil stackUtils = new StackUtil(stack);
				
				added = stackUtils.addToRandomInventory(this,
						Orientations.Unknown);

				if (!added || stackUtils.items.stackSize > 0) {
					added = Utils.addToRandomPipeEntry(this,
							Orientations.Unknown, stackUtils.items);
				}

				// Last, throw the object away

				if (!added) {
					float f = worldObj.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = worldObj.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = worldObj.rand.nextFloat() * 0.8F + 0.1F;

					EntityItem entityitem = new EntityItem(worldObj,
							(float) xCoord + f, (float) yCoord + f1 + 0.5F,
							(float) zCoord + f2, stackUtils.items);

					float f3 = 0.05F;
					entityitem.motionX = (float) worldObj.rand
					.nextGaussian() * f3;
					entityitem.motionY = (float) worldObj.rand
					.nextGaussian() * f3 + 1.0F;
					entityitem.motionZ = (float) worldObj.rand
					.nextGaussian() * f3;
					worldObj.spawnEntityInWorld(entityitem);
				}				
			}
					
			worldObj.setBlockWithNotify((int) i, (int) j, (int) k, 0);
		}		
	}
	
	private boolean blockDig (int blockID) {
		return blockID == Block.bedrock.blockID
				|| blockID == Block.lavaStill.blockID
				|| blockID == Block.lavaMoving.blockID;
	}
	
	private boolean canDig(int blockID) {
		return !blockDig(blockID) 
				&& !API.softBlock(blockID)
				&& blockID != Block.snow.blockID;
	}
	
	@Override
	public void invalidate () {		
		destroy ();
	}
	
	@Override
	public void destroy () {
		if (arm != null) {
			arm.setEntityDead ();
		}
		
		box.deleteLasers();
		arm = null;
	}

	@Override
	public boolean isActive() {
		return isDigging;
	}
	
	private void setBoundaries (boolean useDefault) {
		IAreaProvider a = null;
		
		if (!useDefault) {
			a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
				zCoord);						
		}
		
		if (a == null) {
			a = new DefaultAreaProvider(xCoord, yCoord, zCoord, xCoord + 10,
					yCoord + 4, zCoord + 10);
			
			useDefault = true;
		}
		
		int xSize = a.xMax() - a.xMin() + 1;
		int ySize = a.yMax() - a.yMin() + 1;
		int zSize = a.zMax() - a.zMin() + 1;
		
		if (xSize < 3 || zSize < 3) {
			a = new DefaultAreaProvider(xCoord, yCoord, zCoord, xCoord + 10,
					yCoord + 4, zCoord + 10);
			
			useDefault = true;
		}
		
		xSize = a.xMax() - a.xMin() + 1;
		ySize = a.yMax() - a.yMin() + 1;
		zSize = a.zMax() - a.zMin() + 1;
		
		box.initialize(a);
		
		if (ySize < 5) {
			ySize = 5;
			box.yMax = box.yMin + ySize - 1;
		}
		
		if (useDefault) {
			int xMin = 0, zMin = 0;
			
			Orientations o = Orientations.values()[worldObj.getBlockMetadata(
					xCoord, yCoord, zCoord)].reverse();

			switch (o) {
			case XPos:
				xMin = xCoord + 1;
				zMin = zCoord - 4 - 1;
				break;
			case XNeg:
				xMin = xCoord - 9 - 2;
				zMin = zCoord - 4 - 1;
				break;
			case ZPos:
				xMin = xCoord - 4 - 1;
				zMin = zCoord + 1;
				break;
			case ZNeg:
				xMin = xCoord - 4 - 1;
				zMin = zCoord - 9 - 2;
				break;
			}
			
			box.initialize(xMin, yCoord, zMin, xMin + xSize - 1, yCoord + ySize
					- 1, zMin + zSize - 1);
		}				
		
		a.removeFromWorld();
	}
	
	private void initializeBluePrintBuilder () {
		BluePrint bluePrint = new BluePrint(box.sizeX(), box.sizeY(), box.sizeZ());	
	
		for (int i = 0; i < bluePrint.sizeX; ++i) {
			for (int j = 0; j < bluePrint.sizeY; ++j) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					bluePrint.setBlockId(i, j, k, 0);
				}
			}
		}

		for (int it = 0; it < 2; it++) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				bluePrint.setBlockId(i, it * (box.sizeY() - 1), 0,
						BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(i, it * (box.sizeY() - 1), bluePrint.sizeZ - 1,
						BuildCraftFactory.frameBlock.blockID);
			}

			for (int k = 0; k < bluePrint.sizeZ; ++k) {
				bluePrint.setBlockId(0, it * (box.sizeY() - 1), k,
						BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(bluePrint.sizeX - 1, it * (box.sizeY() - 1), k,
						BuildCraftFactory.frameBlock.blockID);

			}
		}

		for (int h = 1; h < box.sizeY(); ++h) {
			bluePrint.setBlockId(0, h, 0,
					BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(0, h, bluePrint.sizeZ - 1,
					BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h, 0,
					BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h,
					bluePrint.sizeZ - 1,
					BuildCraftFactory.frameBlock.blockID);
		}
		
		bluePrintBuilder = new BluePrintBuilder(bluePrint, box.xMin, yCoord,
				box.zMin);
	}
	
	@Override
	public void postPacketHandling (PacketUpdate packet) { 		
		super.postPacketHandling(packet);
	
		createUtilsIfNeeded();
		
		if (arm != null) {
			arm.setHeadPosition(headPosX, headPosY, headPosZ);
			arm.setTarget(targetX, targetY, targetZ);
			arm.speed = speed;
		}
	}
	
	public void initialize() {
		super.initialize();

		if (!APIProxy.isClient(worldObj)) {
			createUtilsIfNeeded();
		}
		
		sendNetworkUpdate();
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
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

}
