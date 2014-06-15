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
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.utils.Utils;

public class BlockMarker extends BlockContainer {

	public BlockMarker() {
		super(Material.circuits);

		setLightLevel(0.5F);
		setCreativeTab(CreativeTabBuildCraft.ITEMS.get());
	}

	private AxisAlignedBB getBoundingBox(int meta) {
		double w = 0.15;
		double h = 0.65;

		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		switch (dir) {
			case DOWN:
				return AxisAlignedBB.getBoundingBox(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
			case UP:
				return AxisAlignedBB.getBoundingBox(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
			case SOUTH:
				return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
			case NORTH:
				return AxisAlignedBB.getBoundingBox(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
			case EAST:
				return AxisAlignedBB.getBoundingBox(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
			default:
				return AxisAlignedBB.getBoundingBox(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		AxisAlignedBB bBox = getBoundingBox(meta);
		bBox.offset(x, y, z);
		return bBox;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		AxisAlignedBB bb = getBoundingBox(meta);
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.markerModel;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileMarker();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (entityplayer.inventory.getCurrentItem() != null
				&& entityplayer.inventory.getCurrentItem().getItem() instanceof ItemMapLocation) {
			return false;
		}

		TileEntity tile = world.getTileEntity(i, j, k);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).tryConnection();
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		return null;
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
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileMarker) {
			((TileMarker) tile).updateSignals();
		}
		dropTorchIfCantStay(world, x, y, z);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return BuildersProxy.canPlaceTorch(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		return side;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		dropTorchIfCantStay(world, x, y, z);
	}

	private void dropTorchIfCantStay(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!canPlaceBlockOnSide(world, x, y, z, meta)) {
			dropBlockAsItem(world, x, y, z, 0, 0);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon("buildcraft:blockMarker");
	}
}
