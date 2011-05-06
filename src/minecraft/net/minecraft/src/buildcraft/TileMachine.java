package net.minecraft.src.buildcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TileMachine extends TileEntity implements IArmListener {		
	boolean isDigging = true;

	static final int fieldSize = BlockMachine.MINING_FIELD_SIZE
			* BlockMachine.MINING_FIELD_SIZE;
	
	boolean inProcess = false;
	
	EntityMechanicalArm arm;
	
	int xMin, zMin;
	
	boolean loadArm = false;
	
	int targetX, targetY, targetZ;
	
	BluePrintBuilder bluePrintBuilder;
	
	public TileMachine() {
		
	}

	public TileMachine(int xMin, int zMin) {
		this ();
		
		this.xMin = xMin;
		this.zMin = zMin;
	}
	
	public void addToPipe(TilePipe pipe, Item item, Orientations orientation) {		
		World world = ModLoader.getMinecraftInstance().theWorld;
		
		Position itemPos = new Position (xCoord, yCoord, zCoord);
		
		// move to the center of the machine
		
		itemPos.i += 0.50;
		itemPos.j += 0.50;
		itemPos.k += 0.50;		
		
		// move to the beginig of the pipe
		
		itemPos.orientation = orientation;
		itemPos.moveForwards(0.5);
		itemPos.moveDown(0.1);
		
		EntityPassiveItem entityitem = new EntityPassiveItem(
				world, (float) itemPos.i,
				(float) itemPos.j, (float) itemPos.k,
				new ItemStack(item, 1));
		
		world.entityJoinedWorld(entityitem);
				
		pipe.entityEntering(entityitem, itemPos.orientation);		
	}
	
	public void work(Minecraft minecraft) {
		if (bluePrintBuilder == null) {
			bluePrintBuilder = new BluePrintBuilder(worldObj, BlockMachine.bluePrint, xMin, yCoord, zMin);
		}
		
		if (!bluePrintBuilder.done) {
			BlockContents contents = bluePrintBuilder.findNextBlock();
			
			if (contents != null) {
				worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
						contents.blockId);
			}
			
			return;
		}
		
		if (inProcess) {
			return;
		}
		
		if (!isDigging) {
			return;
		}		
		
		World world = minecraft.theWorld;
		
		if (arm == null) {
			arm = new EntityMechanicalArm(world, xMin + Utils.pipeMaxSize,
					yCoord + 4 + Utils.pipeMinSize, zMin + Utils.pipeMaxSize,
					BlockMachine.MINING_FIELD_SIZE + Utils.pipeMinSize * 2,
					BlockMachine.MINING_FIELD_SIZE + Utils.pipeMinSize * 2);
			
			arm.listener = this;
			loadArm = true;
		}
		
		if (loadArm) {
			arm.joinToWorld(worldObj);
			loadArm = false;
		}
		
		boolean[][] blockedColumns = new boolean[BlockMachine.MINING_FIELD_SIZE][BlockMachine.MINING_FIELD_SIZE];
		
		for (int searchX = 0; searchX < BlockMachine.MINING_FIELD_SIZE; ++searchX) {
			for (int searchZ = 0; searchZ < BlockMachine.MINING_FIELD_SIZE; ++searchZ) {
				blockedColumns [searchX][searchZ] = false;
			}
		}
		
		boolean found = false;
		//  look for the next block to dig
		
		for (int searchY = yCoord + 3; searchY >= 0 && !found; --searchY) {
			for (int searchX = 0; searchX < BlockMachine.MINING_FIELD_SIZE && !found; ++searchX) {
				for (int searchZ = 0; searchZ < BlockMachine.MINING_FIELD_SIZE && !found; ++searchZ) {
					if (!blockedColumns [searchX][searchZ]) {
						int bx = xMin + searchX + 1, by = searchY, bz = zMin + searchZ + 1;
						
						int blockId = world.getBlockId(bx, by, bz);
						
						if (blockDig (blockId)) {		
							blockedColumns [searchX][searchZ] = true;						
						} else if (canDig(blockId)) {
							arm.setTarget (bx, by + 1, bz);
							
							targetX = bx;
							targetY = by;
							targetZ = bz;
							
							found = true;
						}
					}
				}
			}
		}

		if (!found) {
			isDigging = false;
		}
		
		inProcess = true;		
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);		

		xMin = nbttagcompound.getInteger("xMin");
		zMin = nbttagcompound.getInteger("zMin");
		targetX = nbttagcompound.getInteger("targetX");
		targetY = nbttagcompound.getInteger("targetY");
		targetZ = nbttagcompound.getInteger("targetZ");
		
		mod_BuildCraft.getInstance().machineBlock.workingMachines.put(
				new BlockIndex(xCoord, yCoord, zCoord), this);
		
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
						Utils.damageDropped(blockId));

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
							(float) i + f,
							(float) j + f1 + 0.5F,
							(float) k + f2,
							new ItemStack(
									idDropped,
									1,
									Utils.damageDropped(blockId)));

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
	


}
