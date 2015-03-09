/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.utils.Utils;

public class BlockFiller extends BlockBuildCraft {
	private IIcon textureTopOff;

	public BlockFiller() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(BCCreativeTab.get("main"));
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		if (entityplayer.isSneaking()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, i, j, k);
		}
		return true;

	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconAbsolute(IBlockAccess world, int x, int y, int z, int side, int metadata) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null && tile instanceof TileFiller) {
			TileFiller filler = (TileFiller) tile;
			if (side != 1) {
				if (filler.currentPattern != null) {
					return filler.currentPattern.getIcon();
				}
			} else {
				if (!filler.hasWork()) {
					return textureTopOff;
				}
			}
		}

		return super.getIconAbsolute(side, metadata);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFiller();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		super.registerBlockIcons(register);
	    textureTopOff = register.registerIcon("buildcraftbuilders:fillerBlock/top_off");
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
