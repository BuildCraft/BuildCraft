package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.DefaultAreaProvider;
import net.minecraft.src.buildcraft.core.IAreaProvider;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class TileQuarry extends TileEntity implements IArmListener, IMachine {		
	boolean isDigging = false;
	
	private static int latency = 20;
	
	boolean inProcess = false;
	
	EntityMechanicalArm arm;
	
	private int xMin, zMin;
	private int xSize, ySize, zSize;
	
	boolean loadArm = false;
	
	int targetX, targetY, targetZ;
	
	BluePrintBuilder bluePrintBuilder;
	
	public TileQuarry() {
		
	}
	
    public void updateEntity()
    {
    	createUtilsIfNeeded ();    	    	
    }
    
    public void createUtilsIfNeeded () {
    	if (bluePrintBuilder == null) {
    		setBoundaries();
    		initializeBluePrintBuilder();
    	}
    	
    	bluePrintBuilder.findNextBlock(worldObj);
    	
    	if (bluePrintBuilder.done) {
    		if (arm == null) {
				arm = new EntityMechanicalArm
				(worldObj,
				xMin + Utils.pipeMaxSize,
				yCoord + bluePrintBuilder.bluePrint.sizeY - 1 + Utils.pipeMinSize,
				zMin + Utils.pipeMaxSize,
				bluePrintBuilder.bluePrint.sizeX - 2 + Utils.pipeMinSize * 2,
				bluePrintBuilder.bluePrint.sizeZ - 2 + Utils.pipeMinSize * 2);

    			arm.listener = this;
    			loadArm = true;
    		}

    		if (loadArm) {
    			arm.joinToWorld(worldObj);
    			loadArm = false;
    			
    			if (findTarget(false)) {    				
    	    		isDigging = true;
    	    	}
    		}
    	} else {
    		isDigging = true;
    	}
    }
	
	boolean lastPower;
	long lastWork = 0;
	
	public void checkPower() {

		boolean currentPower = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);

		if (currentPower != lastPower) {
			lastPower = currentPower;

			work();
		}		
	}
	
	public void work() {				
	    if (worldObj.getWorldTime() - lastWork < latency) {
	    	return;
	    }
	    
	    createUtilsIfNeeded();
	    
	    if (bluePrintBuilder == null) {
	    	return;
	    }
	    
		if (!bluePrintBuilder.done) {
			lastWork = worldObj.getWorldTime();
			BlockContents contents = bluePrintBuilder.findNextBlock(worldObj);
			
			if (contents != null) {		
				int blockId = worldObj.getBlockId(contents.x, contents.y, contents.z);
				
				worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
						contents.blockId);
				
				if (blockId != 0) {
					Block.blocksList[blockId].dropBlockAsItem(
							worldObj,
							contents.x, contents.y, contents.z, blockId);
				}				
			}
			
			return;
		} 	   
		
		if (inProcess) {
			return;
		}
		
		if (!isDigging) {
			return;
		}		
				
		
		if (!findTarget(true)) {
			arm.setTarget (xMin + arm.sizeX / 2, yCoord + 2, zMin + arm.sizeX / 2);
			isDigging = false;			
		}
		
		inProcess = true;		
	}

	public boolean findTarget (boolean doSet) {
		boolean[][] blockedColumns = new boolean[bluePrintBuilder.bluePrint.sizeX - 2][bluePrintBuilder.bluePrint.sizeZ - 2];
		
		for (int searchX = 0; searchX < bluePrintBuilder.bluePrint.sizeX - 2; ++searchX) {
			for (int searchZ = 0; searchZ < bluePrintBuilder.bluePrint.sizeZ - 2; ++searchZ) {
				blockedColumns [searchX][searchZ] = false;
			}
		}
		
		for (int searchY = yCoord + 3; searchY >= 0; --searchY) {
			for (int searchX = 0; searchX < bluePrintBuilder.bluePrint.sizeX - 2; ++searchX) {
				for (int searchZ = 0; searchZ < bluePrintBuilder.bluePrint.sizeZ - 2; ++searchZ) {
					if (!blockedColumns [searchX][searchZ]) {
						int bx = xMin + searchX + 1, by = searchY, bz = zMin + searchZ + 1;
						
						int blockId = worldObj.getBlockId(bx, by, bz);
						
						if (blockDig (blockId)) {		
							blockedColumns [searchX][searchZ] = true;						
						} else if (canDig(blockId)) {
							if (doSet) {
								arm.setTarget (bx, by + 1, bz);

								targetX = bx;
								targetY = by;
								targetZ = bz;
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

		if (nbttagcompound.hasKey("xSize")) {
			xMin = nbttagcompound.getInteger("xMin");
			zMin = nbttagcompound.getInteger("zMin");

			xSize = nbttagcompound.getInteger("xSize");
			ySize = nbttagcompound.getInteger("ySize");
			zSize = nbttagcompound.getInteger("zSize");
		} else {
			// This is a legacy save, compute boundaries
			
			setBoundaries();
		}
		
		initializeBluePrintBuilder();
		
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
		
		nbttagcompound.setInteger("xMin", xMin);
		nbttagcompound.setInteger("zMin", zMin);
		
		nbttagcompound.setInteger("xSize", xSize);
		nbttagcompound.setInteger("ySize", ySize);
		nbttagcompound.setInteger("zSize", zSize);
		
		nbttagcompound.setInteger("targetX", targetX);
		nbttagcompound.setInteger("targetY", targetY);
		nbttagcompound.setInteger("targetZ", targetZ);
		nbttagcompound.setBoolean("hasArm", arm != null);
		
		if (arm != null) {
			NBTTagCompound armStore = new NBTTagCompound();
			nbttagcompound.setTag("arm", armStore);
			arm.writeToNBT(armStore);
		}
	}
	
	
	@Override
	public void positionReached(EntityMechanicalArm arm) {
		int i = targetX;
		int j = targetY;
		int k = targetZ;
		
		int blockId = worldObj.getBlockId((int) i, (int) j, (int) k);
		
		if (canDig(blockId)) {			
			lastWork = worldObj.getWorldTime();
			
			// Share this with mining well!			
			
			Block block = Block.blocksList[blockId];
			
			int idDropped = block.idDropped(blockId,
					worldObj.rand);

			if (idDropped >= Item.itemsList.length
					|| Item.itemsList[idDropped] == null) {
				return;
			}

			int itemQuantity = Block.blocksList[blockId]
					.quantityDropped(worldObj.rand);

			for (int q = 0; q < itemQuantity; ++q) {
				boolean added = false;

				ItemStack items = new ItemStack(idDropped, 1,
						BuildCraftBlockUtil.damageDropped(worldObj, (int) i,
								(int) j, (int) k));

				// First, try to add to a nearby chest

				added = Utils.addToRandomInventory(this, Orientations.Unknown,
						items);

				if (!added) {
					added = Utils.addToRandomPipeEntry(this,
							Orientations.Unknown, items);
				}

				// Last, throw the object away

				if (!added) {
					float f = worldObj.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = worldObj.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = worldObj.rand.nextFloat() * 0.8F + 0.1F;										
										

					EntityItem entityitem = new EntityItem(
							worldObj,
							(float) xCoord + f,
							(float) yCoord + f1 + 0.5F,
							(float) zCoord + f2,
							new ItemStack(
									idDropped,
									1,
									BuildCraftBlockUtil.damageDropped(worldObj,
											(int) i, (int) j, (int) k)));

					float f3 = 0.05F;
					entityitem.motionX = (float) worldObj.rand.nextGaussian()
							* f3;
					entityitem.motionY = (float) worldObj.rand.nextGaussian()
							* f3 + 1.0F;
					entityitem.motionZ = (float) worldObj.rand.nextGaussian()
							* f3;
					worldObj.entityJoinedWorld(entityitem);
				}
			}
			
			worldObj.setBlockWithNotify((int) i, (int) j, (int) k, 0);
		}

		inProcess = false;
	}
	
	boolean blockDig (int blockID) {
		return blockID == Block.bedrock.blockID
				|| blockID == Block.lavaStill.blockID
				|| blockID == Block.lavaMoving.blockID;
	}
	
	boolean canDig(int blockID) {
		return !blockDig(blockID) && blockID != 0
				&& blockID != Block.waterMoving.blockID
				&& blockID != Block.waterStill.blockID
				&& blockID != Block.snow.blockID
				&& Block.blocksList [blockID] != null;
	}
	
	public void delete () {
		if (arm != null) {
			arm.setEntityDead ();
		}
	}

	@Override
	public boolean isActive() {
		return isDigging;
	}
	
	private void setBoundaries () {
		boolean useDefault = false;
		
		IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
				zCoord);	
		
		if (a == null) {
			a = new DefaultAreaProvider (1, 1, 1, 11, 5, 11);
			
			useDefault = true;
		}
		
		xSize = a.xMax() - a.xMin() + 1;
		ySize = a.yMax() - a.yMin() + 1;
		zSize = a.zMax() - a.zMin() + 1;
		
		if (xSize < 3 || zSize < 3) {
			a = new DefaultAreaProvider (1, 1, 1, 11, 5, 11);
			
			useDefault = true;
		}
		
		xSize = a.xMax() - a.xMin() + 1;
		ySize = a.yMax() - a.yMin() + 1;
		zSize = a.zMax() - a.zMin() + 1;
		
		if (ySize < 5) {
			ySize = 5;
		}
		
		if (useDefault) {
			Orientations o = Orientations.values()[worldObj.getBlockMetadata(
					xCoord, yCoord, zCoord)].reverse();

			if (xMin == -1) {
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
			}
		} else {
			xMin = a.xMin();
			zMin = a.zMin();
		}
	}
	
	private void initializeBluePrintBuilder () {
		BluePrint bluePrint = new BluePrint(xSize, ySize, zSize);	
	
		for (int i = 0; i < bluePrint.sizeX; ++i) {
			for (int j = 0; j < bluePrint.sizeY; ++j) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					bluePrint.setBlockId(i, j, k, 0);
				}
			}
		}

		for (int it = 0; it < 2; it++) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				bluePrint.setBlockId(i, it * (ySize - 1), 0,
						mod_BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(i, it * (ySize - 1), bluePrint.sizeZ - 1,
						mod_BuildCraftFactory.frameBlock.blockID);
			}

			for (int k = 0; k < bluePrint.sizeZ; ++k) {
				bluePrint.setBlockId(0, it * (ySize - 1), k,
						mod_BuildCraftFactory.frameBlock.blockID);
				bluePrint.setBlockId(bluePrint.sizeX - 1, it * (ySize - 1), k,
						mod_BuildCraftFactory.frameBlock.blockID);

			}
		}

		for (int h = 1; h < ySize; ++h) {
			bluePrint.setBlockId(0, h, 0,
					mod_BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(0, h, bluePrint.sizeZ - 1,
					mod_BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h, 0,
					mod_BuildCraftFactory.frameBlock.blockID);
			bluePrint.setBlockId(bluePrint.sizeX - 1, h,
					bluePrint.sizeZ - 1,
					mod_BuildCraftFactory.frameBlock.blockID);
		}
		
		bluePrintBuilder = new BluePrintBuilder(bluePrint, xMin, yCoord, zMin);
	}

}
