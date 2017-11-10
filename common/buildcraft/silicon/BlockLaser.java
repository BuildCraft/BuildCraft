/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.ICustomHighlight;

public class BlockLaser extends BlockBuildCraft implements ICustomHighlight {

	private static final AxisAlignedBB[][] boxes = {
			{AxisAlignedBB.getBoundingBox(0.0, 0.75, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.3125, 0.1875, 0.3125, 0.6875, 0.75, 0.6875)}, // -Y
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 0.25, 1.0), AxisAlignedBB.getBoundingBox(0.3125, 0.25, 0.3125, 0.6875, 0.8125, 0.6875)}, // +Y
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.75, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.3125, 0.3125, 0.1875, 0.6875, 0.6875, 0.75)}, // -Z
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.25), AxisAlignedBB.getBoundingBox(0.3125, 0.3125, 0.25, 0.6875, 0.6875, 0.8125)}, // +Z
			{AxisAlignedBB.getBoundingBox(0.75, 0.0, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.1875, 0.3125, 0.3125, 0.75, 0.6875, 0.6875)}, // -X
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 0.25, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.25, 0.3125, 0.3125, 0.8125, 0.6875, 0.6875)} // +X
	};

	public BlockLaser() {
		super(Material.iron);
		setHardness(10F);
		setCreativeTab(BCCreativeTab.get("main"));
	}

	@Override
	public AxisAlignedBB[] getBoxes(World wrd, int x, int y, int z, EntityPlayer player) {
		return boxes[wrd.getBlockMetadata(x, y, z)];
	}

	@Override
	public double getExpansion() {
		return 0.0075;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, int x, int y, int z, Vec3 origin, Vec3 direction) {
		AxisAlignedBB[] aabbs = boxes[wrd.getBlockMetadata(x, y, z)];
		MovingObjectPosition closest = null;
		for (AxisAlignedBB aabb : aabbs) {
			MovingObjectPosition mop = aabb.getOffsetBoundingBox(x, y, z).calculateIntercept(origin, direction);
			if (mop != null) {
				if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
					closest = mop;
				} else {
					closest = mop;
				}
			}
		}
		if (closest != null) {
			closest.blockX = x;
			closest.blockY = y;
			closest.blockZ = z;
		}
		return closest;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addCollisionBoxesToList(World wrd, int x, int y, int z, AxisAlignedBB mask, List list, Entity ent) {
		AxisAlignedBB[] aabbs = boxes[wrd.getBlockMetadata(x, y, z)];
		for (AxisAlignedBB aabb : aabbs) {
			AxisAlignedBB aabbTmp = aabb.getOffsetBoundingBox(x, y, z);
			if (mask.intersectsWith(aabbTmp)) {
				list.add(aabbTmp);
			}
		}
	}

	@Override
	public int getRenderType() {
		return SiliconProxy.laserBlockModel;
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
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileLaser();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
		return getIcon(side, access.getBlockMetadata(x, y, z));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int i, int j) {
		if (i == (j ^ 1)) {
			return icons[0][0];
		} else if (i == j) {
			return icons[0][1];
		} else {
			return icons[0][2];
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);

		int retMeta = meta;

		if (side <= 6) {
			retMeta = side;
		}

		return retMeta;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}
}
