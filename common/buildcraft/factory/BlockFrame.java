/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.core.CoreConstants;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.utils.Utils;

public class BlockFrame extends Block implements IFramePipeConnection {

	public BlockFrame() {
		super(Material.glass);
		setHardness(0.5F);
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
	public Item getItemDropped(int i, Random random, int j) {
		return null;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.legacyPipeModel;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax = CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

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

		return AxisAlignedBB.getBoundingBox((double) i + xMin, (double) j + yMin, (double) k + zMin, (double) i + xMax, (double) j + yMax, (double) k + zMax);
	}

	@Override
	@SuppressWarnings({ "all" })
	// @Override (client only)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		return getCollisionBoundingBoxFromPool(world, i, j, k);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i - 1, j, k)) {
			setBlockBounds(0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i + 1, j, k)) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 1.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j - 1, k)) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j + 1, k)) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k - 1)) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, i, j, k, i, j, k + 1)) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3 vec3d, Vec3 vec3d1) {
		float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax = CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

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
		return blockAccess.getBlock(x2, y2, z2) == this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    blockIcon = par1IconRegister.registerIcon("buildcraft:blockFrame");
	}
}
