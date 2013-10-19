/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockMarker extends BlockContainer {

	public BlockMarker(int i) {
		super(i, Material.circuits);

		setLightValue(0.5F);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
	}

	private AxisAlignedBB getBoundingBox(int meta) {
		double w = 0.15;
		double h = 0.65;

		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		switch (dir) {
			case DOWN:
				return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
			case UP:
				return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
			case SOUTH:
				return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
			case NORTH:
				return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
			case EAST:
				return AxisAlignedBB.getAABBPool().getAABB(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
			default:
				return AxisAlignedBB.getAABBPool().getAABB(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
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
	public TileEntity createNewTileEntity(World var1) {
		return new TileMarker();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		((TileMarker) world.getBlockTileEntity(i, j, k)).tryConnection();
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
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
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockId) {
		((TileMarker) world.getBlockTileEntity(x, y, z)).updateSignals();
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
			dropBlockAsItem(world, x, y, z, BuildCraftBuilders.markerBlock.blockID, 0);
			world.setBlock(x, y, z, 0);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon("buildcraft:blockMarker");
	}
}
