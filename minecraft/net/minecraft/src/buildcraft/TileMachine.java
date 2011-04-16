package net.minecraft.src.buildcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TileMachine extends TileEntity {

	// TODO: use xCoord yCoord zCoord instead
	int i, j, k;
	int depth;
	int step;
	Orientations orientation;
	
	boolean isDigging = true;

	static final int fieldSizeX = 3;
	static final int fieldSizeZ = 3;
	static final int fieldSize = fieldSizeX * fieldSizeZ;
	
	public TileMachine() {
		
	}

	public TileMachine(int ci, int cj, int ck, Orientations corientation) {
		i = ci;
		j = cj;
		k = ck;
		depth = 0;
		step = 0;
		orientation = corientation;
	}
	
	public void addToPipe(TilePipe pipe, Item item, Orientations orientation) {		
		World world = ModLoader.getMinecraftInstance().theWorld;
		
		Position itemPos = new Position (i, j, k);
		
		// move to the center of the machine
		
		itemPos.i += 0.50;
		itemPos.j += 0.50;
		itemPos.k += 0.50;		
		
		// move to the beginig of the pipe
		
		itemPos.orientation = orientation;
		itemPos.moveForwards(0.5);
		itemPos.moveDown(0.1);
		
		EntityItem entityitem = new EntityPassiveItem(
				world, (float) itemPos.i,
				(float) itemPos.j, (float) itemPos.k,
				new ItemStack(item, 1));
		
		world.entityJoinedWorld(entityitem);
				
		pipe.entityEntering(entityitem, itemPos.orientation);		
	}

	/**
	 * Attempts to add the item in parameter to the chest given in parameter.
	 * Returns true if succeed, false otherwise.
	 * 
	 * @param chest
	 * @param blockId
	 */
	public boolean addToChest(TileEntityChest chest, Item item) {
		if (item == null) {
			return false;
		}

		// First, look for a similar pile

		for (int j = 0; j < chest.getSizeInventory(); ++j) {
			ItemStack stack = chest.getStackInSlot(j);
			if (stack != null) {
				if (stack.getItem() == item
						&& stack.stackSize < stack.getMaxStackSize()) {
					stack.stackSize++;

					return true;
				}
			}
		}

		// If none, then create a new thing

		for (int j = 0; j < chest.getSizeInventory(); ++j) {
			ItemStack stack = chest.getStackInSlot(j);
			if (stack == null) {
				stack = new ItemStack(item, 1);
				chest.setInventorySlotContents(j, stack);

				return true;
			}
		}

		// If the chest if full, return false

		return false;
	}
	
	public void work(Minecraft minecraft) {
		if (!isDigging) {
			return;
		}

		World world = minecraft.theWorld;

		int diffX = step % fieldSizeX;
		int diffZ = step / fieldSizeZ;
		Position pos = new Position(i, j - depth, k, orientation);

		pos.moveRight(fieldSizeX / 2);
		pos.moveForwards(2);

		pos.moveLeft(diffX);
		pos.moveForwards(diffZ);

		step++;

		if (step >= fieldSize) {
			depth++;
			step = 0;
		}

		if (j - depth <= 0) {
			isDigging = false;

			return;
		}

		int blockId = world.getBlockId((int) pos.i, (int) pos.j, (int) pos.k);

		if (blockId == Block.bedrock.blockID
				|| blockId == Block.lavaStill.blockID
				|| blockId == Block.lavaMoving.blockID) {

			isDigging = false;

			return;
		}

		if (blockId == 0 || blockId >= Block.blocksList.length
				|| Block.blocksList[blockId] == null) {
			return;
		}

		int idDropped = Block.blocksList[blockId]
				.idDropped(blockId, world.rand);

		if (idDropped >= Item.itemsList.length
				|| Item.itemsList[idDropped] == null) {
			return;
		}

		Item item = Item.itemsList[idDropped];
		int itemQuantity = Block.blocksList[blockId]
				.quantityDropped(world.rand);

		for (int q = 0; q < itemQuantity; ++q) {
			boolean added = false;

			// First, try to add to a nearby chest
			
			for (int i_next = i - 1; i_next <= i + 1 && !added; ++i_next) {
				for (int j_next = j - 1; j_next <= j + 1 && !added; ++j_next) {
					for (int k_next = k - 1; k_next <= k + 1 && !added; ++k_next) {

						if (minecraft.theWorld.getBlockId(i_next, j_next,
								k_next) == Block.crate.blockID) {
							added = addToChest(
									(TileEntityChest) world.getBlockTileEntity(
											i_next, j_next, k_next), item);
						}
					}
				}
			}
			
			// Second, try to add in a nearby pipe
			// TODO: should list all the pipes and pick up one randomly
			// factorize that code somewhere (needed for the pipes too).
			
			int pipeId = mod_BuildCraft.getInstance().pipeBlock.blockID;
			
			//  TODO: use the same loop for finding a chest?
			for (int o = 1; o <= 6; ++o) {
				Position posPipe = new Position (i, j, k, Orientations.values()[o]);
				posPipe.moveForwards(1);
				
				if (minecraft.theWorld.getBlockId((int) posPipe.i, (int) posPipe.j,
						(int) posPipe.k) == pipeId) {
					added = true;
					
					addToPipe((TilePipe) world.getBlockTileEntity(
							(int) posPipe.i, (int) posPipe.j, (int) posPipe.k),
							item, Orientations.values()[o]);
					
					break;
				}
			}
			
			
			
			// Last, throw the object away

			if (!added) {
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityitem = new EntityItem(world, (float) i + f,
						(float) j + f1 + 0.5F, (float) k + f2, new ItemStack(
								item, 1));

				float f3 = 0.05F;
				entityitem.motionX = (float) world.rand.nextGaussian() * f3;
				entityitem.motionY = (float) world.rand.nextGaussian() * f3
						+ 1.0F;
				entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
				world.entityJoinedWorld(entityitem);
			}
		}

		world.setBlock((int) pos.i, (int) pos.j, (int) pos.k, 0);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);		

		i = nbttagcompound.getInteger("i");
		j = nbttagcompound.getInteger("j");
		k = nbttagcompound.getInteger("k");
		depth = nbttagcompound.getInteger("depth");
		orientation = Orientations.values()[nbttagcompound
				.getInteger("orientation")];
		step = nbttagcompound.getInteger("step");

		mod_BuildCraft.getInstance().machineBlock.workingMachines.put(
				new BlockIndex(i, j, k), this);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);		

		nbttagcompound.setInteger("i", i);
		nbttagcompound.setInteger("j", j);
		nbttagcompound.setInteger("k", k);
		nbttagcompound.setInteger("depth", depth);
		nbttagcompound.setInteger("orientation", orientation.ordinal());
		nbttagcompound.setInteger("step", step);
	}

}
