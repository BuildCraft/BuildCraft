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
		
		EntityPassiveItem entityitem = new EntityPassiveItem(
				world, (float) itemPos.i,
				(float) itemPos.j, (float) itemPos.k,
				new ItemStack(item, 1));
		
		world.entityJoinedWorld(entityitem);
				
		pipe.entityEntering(entityitem, itemPos.orientation);		
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
			
			ItemStack items = new ItemStack(item, 1);

			// First, try to add to a nearby chest

			added = Utils.addToRandomChest(this, Orientations.Unknown, items);
			
			if (!added) {
				added = Utils.addToRandomPipeEntry(this, Orientations.Unknown, items);
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
