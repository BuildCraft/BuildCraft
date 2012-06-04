package net.minecraft.src.buildcraft.core;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;

public abstract class BlockBuildCraft extends BlockContainer {

	protected static boolean keepInventory = false;
	protected Random rand;

	protected BlockBuildCraft(int id, Material material) {
		super(id, material);
		this.rand = new Random();
	}

	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {

		if (APIProxy.isRemote())
			return;

		if (!keepInventory) {

			IInventory tile = (IInventory) world.getBlockTileEntity(i, j, k);
			if (tile != null)
				label0: for (int l = 0; l < tile.getSizeInventory(); l++) {

					ItemStack itemstack = tile.getStackInSlot(l);
					if (itemstack == null)
						continue;

					float f = rand.nextFloat() * 0.8F + 0.1F;
					float f1 = rand.nextFloat() * 0.8F + 0.1F;
					float f2 = rand.nextFloat() * 0.8F + 0.1F;

					do {
						if (itemstack.stackSize <= 0)
							continue label0;
						int i1 = rand.nextInt(21) + 10;
						if (i1 > itemstack.stackSize)
							i1 = itemstack.stackSize;
						ItemStack drop = itemstack.splitStack(i1);
						EntityItem entityitem = new EntityItem(world, i + f, j
								+ f1, k + f2, drop);
						float f3 = 0.05F;
						entityitem.motionX = (float) rand.nextGaussian() * f3;
						entityitem.motionY = (float) rand.nextGaussian() * f3
								+ 0.2F;
						entityitem.motionZ = (float) rand.nextGaussian() * f3;
						world.spawnEntityInWorld(entityitem);

					} while (true);
				}
		}
		super.onBlockRemoval(world, i, j, k);
	}

}
