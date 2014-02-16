/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import buildcraft.builders.blueprints.IBlueprintBuilderAgent;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRobotBuilder extends EntityRobot implements
		IEntityAdditionalSpawnData, IBlueprintBuilderAgent, IInventory {

	ItemStack buildingStack = null;

	public EntityRobotBuilder(World par1World) {
		super (par1World);
	}

	/**
	 * Operate a block break. Return true is the block has indeed been broken.
	 */
	@Override
	public boolean breakBlock (int x, int y, int z) {
		Block block = worldObj.getBlock(x, y, z);

		if (block != null) {
			curBlockDamage += 1 / (block.getBlockHardness(worldObj, x,y, z) * 20);
		}

		if (block != null && curBlockDamage < 1) {
			worldObj.destroyBlockInWorldPartially(getEntityId(), x, y, z,
					(int) (this.curBlockDamage * 10.0F) - 1);

			setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
			showLaser();

			return false;
		} else {
			worldObj.destroyBlockInWorldPartially(getEntityId(), x, y, z, -1);
			worldObj.setBlock(x, y, z, Blocks.air);
			curBlockDamage = 0;

			hideLaser();

			return true;
		}
	}

	@Override
	public boolean buildBlock(int x, int y, int z) {
		if (buildingStack == null) {
			if (worldObj.getBlock(x, y, z) != Blocks.air) {
				breakBlock(x, y, z);
			} else {
				setLaserDestination(x + 0.5F, y + 0.5F, z + 0.5F);
				showLaser();

				buildingStack = getInventory().decrStackSize(0, 1);
				buildEnergy = 0;
			}

			return false;
		} else {
			buildEnergy++;

			if (buildEnergy >= 25) {
				buildingStack.getItem().onItemUse(buildingStack,
						CoreProxy.proxy.getBuildCraftPlayer(worldObj),
						worldObj, x, y - 1, z, 1, 0.0f, 0.0f, 0.0f);

				buildingStack = null;

				hideLaser();
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public IInventory getInventory() {
		return this;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// Fake inventory filled with bricks
		return new ItemStack(Blocks.brick_block);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// Fake inventory filled with bricks
		return new ItemStack(Blocks.brick_block);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getInventoryName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openInventory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub

	}
}
