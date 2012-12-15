/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.BuildCraftCore;
import buildcraft.core.DefaultProps;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.utils.Utils;


public class BlockFrame extends Block implements IFramePipeConnection {

	public BlockFrame(int i) {
		super(i, Material.glass);

		blockIndexInTexture = 16 * 2 + 2;
		setHardness(0.5F);
		setTickRandomly(true);
	}

	@Override
	public void updateTick(World world, int i, int j, int k, Random random) {
		if (world.isRemote)
			return;
		
		int meta = world.getBlockMetadata(i, j, k);
		if (meta == 1 && random.nextInt(10) > 5){
			world.setBlockWithNotify(i, j, k, 0);
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int idDropped(int i, Random random, int j) {
		return -1;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.legacyPipeModel;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i - 1, j, k)) {
			xMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i + 1, j, k)) {
			xMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j - 1, k)) {
			yMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j + 1, k)) {
			yMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k - 1)) {
			zMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k + 1)) {
			zMax = 1.0F;
		}

		return AxisAlignedBB.getBoundingBox((double) i + xMin, (double) j + yMin, (double) k + zMin, (double) i + xMax,
				(double) j + yMax, (double) k + zMax);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		return getCollisionBoundingBoxFromPool(world, i, j, k);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollidingBlockToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, 1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1.0F);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3 vec3d, Vec3 vec3d1) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i - 1, j, k)) {
			xMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i + 1, j, k)) {
			xMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j - 1, k)) {
			yMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j + 1, k)) {
			yMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k - 1)) {
			zMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k + 1)) {
			zMax = 1.0F;
		}

		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

		MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d, vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1, int z1, int x2, int y2, int z2) {
		return blockAccess.getBlockId(x2, y2, z2) == blockID;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
