/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ICustomHighlight;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

import static net.minecraft.util.AxisAlignedBB.getBoundingBox;

public class BlockLaser extends BlockContainer implements ICustomHighlight {

	private static final AxisAlignedBB[][] boxes = {
			{getBoundingBox(0.0, 0.75, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.3125, 0.1875, 0.3125, 0.6875, 0.75, 0.6875)},// -Y
			{getBoundingBox(0.0, 0.0, 0.0, 1.0, 0.25, 1.0), getBoundingBox(0.3125, 0.25, 0.3125, 0.6875, 0.8125, 0.6875)},// +Y
			{getBoundingBox(0.0, 0.0, 0.75, 1.0, 1.0, 1.0), getBoundingBox(0.3125, 0.3125, 0.1875, 0.6875, 0.6875, 0.75)},// -Z
			{getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.25), getBoundingBox(0.3125, 0.3125, 0.25, 0.6875, 0.6875, 0.8125)},// +Z
			{getBoundingBox(0.75, 0.0, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.1875, 0.3125, 0.3125, 0.75, 0.6875, 0.6875)},// -X
			{getBoundingBox(0.0, 0.0, 0.0, 0.25, 1.0, 1.0), getBoundingBox(0.25, 0.3125, 0.3125, 0.8125, 0.6875, 0.6875)} // +X
	};

	@SideOnly(Side.CLIENT)
	private IIcon textureTop, textureBottom, textureSide;

	public BlockLaser() {
		super(Material.iron);
		setHardness(10F);
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
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
	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(World wrd, int x, int y, int z, AxisAlignedBB mask, List list, Entity ent) {
		AxisAlignedBB[] aabbs = boxes[wrd.getBlockMetadata(x, y, z)];
		for (AxisAlignedBB aabb : aabbs) {
			aabb = aabb.getOffsetBoundingBox(x, y, z);
			if (mask.intersectsWith(aabb)) {
				list.add(aabb);
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

	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileLaser();
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (i == ForgeDirection.values()[j].getOpposite().ordinal()) {
			return textureBottom;
		} else if (i == j) {
			return textureTop;
		} else {
			return textureSide;
		}

	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);

		if (side <= 6) {
			meta = side;
		}

		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureTop = par1IconRegister.registerIcon("buildcraft:laser_top");
		textureBottom = par1IconRegister.registerIcon("buildcraft:laser_bottom");
		textureSide = par1IconRegister.registerIcon("buildcraft:laser_side");
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}
}
