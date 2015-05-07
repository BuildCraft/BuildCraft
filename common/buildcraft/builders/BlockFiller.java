/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.BlockBuildCraftLED;
import buildcraft.core.GuiIds;

public class BlockFiller extends BlockBuildCraftLED {
	public BlockFiller() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(BCCreativeTab.get("main"));
		setRotatable(true);
		setPassCount(4);
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
	public int getIconGlowLevel(IBlockAccess access, int x, int y, int z) {
		if (renderPass == 0 || renderPass == 3) {
			return -1;
		} else {
			TileFiller tile = (TileFiller) access.getTileEntity(x, y, z);
			return tile.getIconGlowLevel(renderPass);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFiller();
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

	@Override
	public IIcon getIconAbsolute(IBlockAccess access, int x, int y, int z, int side, int meta) {
		if (renderPass < 3) {
			return super.getIconAbsolute(access, x, y, z, side, meta);
		} else {
			if (side == 2) {
				TileEntity tile = access.getTileEntity(x, y, z);
				if (tile instanceof TileFiller && ((TileFiller) tile).currentPattern != null) {
					return ((TileFiller) tile).currentPattern.getBlockOverlay();
				}
			}
			return null;
		}
	}

	@Override
	public IIcon getIconAbsolute(int side, int meta) {
		if (renderPass < 3) {
			return super.getIconAbsolute(side, meta);
		} else {
			return null;
		}
	}
}
