package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class TileMachine extends TileEntity implements IArmListener, IMachine {		
	boolean isDigging = false;

	static final int fieldSize = BlockMachine.MINING_FIELD_SIZE
			* BlockMachine.MINING_FIELD_SIZE;
	
	boolean inProcess = false;
	
	EntityMechanicalArm arm;
	
	private int xMin, zMin;
	
	boolean loadArm = false;
	
	int targetX, targetY, targetZ;
	
	BluePrintBuilder bluePrintBuilder;
	
	public TileMachine() {
		
	}
	
    public void updateEntity()
    {
    	createUtilsIfNeeded ();    	    	
    }
    
    public void createUtilsIfNeeded () {
    	if (bluePrintBuilder == null) {
    		return;
    	}
    	
    	bluePrintBuilder.findNextBlock(worldObj);
    	
    	if (bluePrintBuilder.done) {
    		if (arm == null) {
    			arm = new EntityMechanicalArm(worldObj, xMin + Utils.pipeMaxSize,
    					yCoord + 4 + Utils.pipeMinSize, zMin + Utils.pipeMaxSize,
    					BlockMachine.MINING_FIELD_SIZE + Utils.pipeMinSize * 2,
    					BlockMachine.MINING_FIELD_SIZE + Utils.pipeMinSize * 2);

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
	    if (worldObj.getWorldTime() - lastWork < 20) {
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
		boolean[][] blockedColumns = new boolean[BlockMachine.MINING_FIELD_SIZE][BlockMachine.MINING_FIELD_SIZE];
		
		for (int searchX = 0; searchX < BlockMachine.MINING_FIELD_SIZE; ++searchX) {
			for (int searchZ = 0; searchZ < BlockMachine.MINING_FIELD_SIZE; ++searchZ) {
				blockedColumns [searchX][searchZ] = false;
			}
		}
		
		for (int searchY = yCoord + 3; searchY >= 0; --searchY) {
			for (int searchX = 0; searchX < BlockMachine.MINING_FIELD_SIZE; ++searchX) {
				for (int searchZ = 0; searchZ < BlockMachine.MINING_FIELD_SIZE; ++searchZ) {
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

		xMin = nbttagcompound.getInteger("xMin");
		zMin = nbttagcompound.getInteger("zMin");
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
		
		bluePrintBuilder = new BluePrintBuilder(BlockMachine.bluePrint, xMin,
				yCoord, zMin);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);		
		
		nbttagcompound.setInteger("xMin", xMin);
		nbttagcompound.setInteger("zMin", zMin);
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
				&& Block.blocksList [blockID] != null;
	}
	
	public void delete () {
		if (arm != null) {
			arm.setEntityDead ();
		}
	}
	
	public void setMinPos (int xMin, int zMin) {
		this.xMin = xMin;
		this.zMin = zMin;
		
		bluePrintBuilder = new BluePrintBuilder(BlockMachine.bluePrint, xMin,
				yCoord, zMin);
	}

}
