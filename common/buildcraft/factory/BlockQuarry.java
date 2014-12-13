/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftFactory;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockQuarry extends BlockBuildCraft {

	IIcon textureTop;
	IIcon textureFront;
	IIcon textureSide;

	public BlockQuarry() {
		super(Material.iron);

		setHardness(10F);
		setResistance(10F);
		// TODO: set proper sound
		//setStepSound(soundAnvilFootstep);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		EnumFacing orientation = Utils.get2dOrientation(entityliving);

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
		if (entityliving instanceof EntityPlayer) {
			TileEntity tile = world.getTileEntity(i, j, k);
			if (tile instanceof TileQuarry) {
				((TileQuarry) tile).placedBy = (EntityPlayer) entityliving;
			}
		}
	}

	@Override
	public IIcon getIcon(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == j && i > 1) {
			return textureFront;
		}

		switch (i) {
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileQuarry();
	}

	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
			return;
		}

		Block block = world.getBlock(i, j, k);

		if (block != BuildCraftFactory.frameBlock) {
			return;
		}

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadataWithNotify(i, j, k, meta | 8, 0);

			EnumFacing[] dirs = EnumFacing.values();

			for (EnumFacing dir : dirs) {
				switch (dir) {
				case UP:
						searchFrames(world, i, j + 1, k);
					break;
				case DOWN:
						searchFrames(world, i, j - 1, k);
					break;
				case SOUTH:
						searchFrames(world, i, j, k + 1);
					break;
				case NORTH:
						searchFrames(world, i, j, k - 1);
					break;
				case EAST:
						searchFrames(world, i + 1, j, k);
					break;
				case WEST:
					default:
						searchFrames(world, i - 1, j, k);
					break;
				}
			}
		}
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (BuildCraftFactory.quarryOneTimeUse) {
			return new ArrayList<ItemStack>();
		}
		return super.getDrops(world, x, y, z, metadata, fortune);
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, Block block, int metadata) {
		if (world.isRemote) {
			return;
		}

		BuildCraftFactory.frameBlock.removeNeighboringFrames(world, i, j, k);

		super.breakBlock(world, i, j, k, block, metadata);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		TileQuarry tile = (TileQuarry) world.getTileEntity(i, j, k);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		// Restart the quarry if its a wrench
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			tile.reinitalize();
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;

		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureSide = par1IconRegister.registerIcon("buildcraft:quarry_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:quarry_top");
		textureFront = par1IconRegister.registerIcon("buildcraft:quarry_front");
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, EnumFacing side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 1;
	}
}
