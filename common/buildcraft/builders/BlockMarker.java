/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.ModelHelper;
import buildcraft.core.utils.Utils;

public class BlockMarker extends BlockBuildCraft {
	
	public BlockMarker() {
		super(Material.circuits, new PropertyEnum[]{FACING_6_PROP});
		setDefaultState(getDefaultState().withProperty(FACING_6_PROP, EnumFacing.UP));
		setLightLevel(0.5F);
		setHardness(0.0F);
		setCreativeTab(CreativeTabBuildCraft.ITEMS.get());
	}

	public static boolean canPlaceTorch(World world, BlockPos pos, EnumFacing side) {
		Block block = world.getBlockState(pos).getBlock();
		return (block.isOpaqueCube() || block.isSideSolid(world, pos, side));
	}

	private AxisAlignedBB getBoundingBox(EnumFacing dir) {
		double w = 0.15;
		double h = 0.65;

		switch (dir) {
			case DOWN:
				return AxisAlignedBB.fromBounds(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
			case UP:
				return AxisAlignedBB.fromBounds(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
			case SOUTH:
				return AxisAlignedBB.fromBounds(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
			case NORTH:
				return AxisAlignedBB.fromBounds(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
			case EAST:
				return AxisAlignedBB.fromBounds(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
			default:
				return AxisAlignedBB.fromBounds(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
		EnumFacing side = (EnumFacing)world.getBlockState(pos).getValue(FACING_6_PROP);
		AxisAlignedBB bBox = getBoundingBox(side);
		bBox.offset(pos.getX(), pos.getY(), pos.getZ());
		return bBox;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		EnumFacing side = (EnumFacing)world.getBlockState(pos).getValue(FACING_6_PROP);
		AxisAlignedBB bb = getBoundingBox(side);
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileMarker();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (entityplayer.inventory.getCurrentItem() != null
				&& entityplayer.inventory.getCurrentItem().getItem() instanceof ItemMapLocation) {
			return false;
		}

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).tryConnection();
		}
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() { return false; }

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).updateSignals();
		}
		dropTorchIfCantStay(world, pos, state);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		return canPlaceTorch(world, pos.offset(side, -1), side);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING_6_PROP, facing);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		dropTorchIfCantStay(world, pos, state);
	}

	private void dropTorchIfCantStay(World world, BlockPos pos, IBlockState state) {
		EnumFacing side = (EnumFacing) state.getValue(FACING_6_PROP);
		if (!canPlaceBlockOnSide(world, pos, side)) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}
}
