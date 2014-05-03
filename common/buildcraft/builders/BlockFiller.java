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
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockFiller extends BlockContainer {

	public IFillerPattern currentPattern;
	private IIcon textureSides;
	private IIcon textureTopOn;
	private IIcon textureTopOff;

	public BlockFiller() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, i, j, k);
		}
		return true;

	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int m = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile != null && tile instanceof TileFiller) {
			TileFiller filler = (TileFiller) tile;
			if (side == 1 || side == 0) {
				if (!filler.isActive()) {
					return textureTopOff;
				} else {
					return textureTopOn;
				}
			} else if (filler.currentPattern != null) {
				return filler.currentPattern.getIcon();
			} else {
				return textureSides;
			}
		}

		return getIcon(side, m);
	}

	@Override
	public IIcon getIcon(int i, int j) {
		if (i == 0 || i == 1) {
			return textureTopOn;
		} else {
			return textureSides;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFiller();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    textureTopOn = par1IconRegister.registerIcon("buildcraft:blockFillerTopOn");
        textureTopOff = par1IconRegister.registerIcon("buildcraft:blockFillerTopOff");
        textureSides = par1IconRegister.registerIcon("buildcraft:blockFillerSides");
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 1;
	}
}
