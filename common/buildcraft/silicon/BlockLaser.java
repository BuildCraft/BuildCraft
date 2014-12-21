/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ICustomHighlight;

public class BlockLaser extends BlockBuildCraft implements ICustomHighlight {

	private static final AxisAlignedBB[][] boxes = {
			{AxisAlignedBB.fromBounds(0.0, 0.75, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.3125, 0.1875, 0.3125, 0.6875, 0.75, 0.6875)}, // -Y
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 0.25, 1.0), AxisAlignedBB.fromBounds(0.3125, 0.25, 0.3125, 0.6875, 0.8125, 0.6875)}, // +Y
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.75, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.3125, 0.3125, 0.1875, 0.6875, 0.6875, 0.75)}, // -Z
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 1.0, 0.25), AxisAlignedBB.fromBounds(0.3125, 0.3125, 0.25, 0.6875, 0.6875, 0.8125)}, // +Z
			{AxisAlignedBB.fromBounds(0.75, 0.0, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.fromBounds(0.1875, 0.3125, 0.3125, 0.75, 0.6875, 0.6875)}, // -X
			{AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 0.25, 1.0, 1.0), AxisAlignedBB.fromBounds(0.25, 0.3125, 0.3125, 0.8125, 0.6875, 0.6875)} // +X
	};

	/*@SideOnly(Side.CLIENT)
	private IIcon textureTop, textureBottom, textureSide;*/

	public BlockLaser() {
		super(Material.iron, new PropertyEnum[]{FACING_6_PROP});
		setHardness(10F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public AxisAlignedBB[] getBoxes(World wrd, BlockPos pos, EntityPlayer player) {
		return boxes[((EnumFacing)wrd.getBlockState(pos).getValue(FACING_PROP)).getIndex()];
	}

	@Override
	public double getExpansion() {
		return 0.0075;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, BlockPos pos, Vec3 origin, Vec3 direction) {
		EnumFacing face;
		AxisAlignedBB[] aabbs = boxes[((EnumFacing)wrd.getBlockState(pos).getValue(FACING_PROP)).getIndex()];
		MovingObjectPosition closest = null;
		for (AxisAlignedBB aabb : aabbs) {
			MovingObjectPosition mop = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(origin, direction);
			if (mop != null) {
				if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
					closest = mop;
				} else {
					closest = mop;
				}
			}
		}
		if (closest != null) {
			closest = new MovingObjectPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()), closest.field_178784_b);
		}
		return closest;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(World wrd, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity ent) {
		AxisAlignedBB[] aabbs = boxes[((EnumFacing)wrd.getBlockState(pos).getValue(FACING_PROP)).getIndex()];
		for (AxisAlignedBB aabb : aabbs) {
			AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
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


	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileLaser();
	}

	/*@Override
	public IIcon getIcon(int i, int j) {
		if (i == (j ^ 1)) {
			return textureBottom;
		} else if (i == j) {
			return textureTop;
		} else {
			return textureSide;
		}

	}*/

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);

		int retMeta = meta;

		if (facing.getIndex() <= 6) {
			retMeta = facing.getIndex();
		}

		return getDefaultState().withProperty(FACING_6_PROP, EnumFacing.getFront(retMeta));
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureTop = par1IconRegister.registerIcon("buildcraft:laser_top");
		textureBottom = par1IconRegister.registerIcon("buildcraft:laser_bottom");
		textureSide = par1IconRegister.registerIcon("buildcraft:laser_side");
	}*/

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}
}
