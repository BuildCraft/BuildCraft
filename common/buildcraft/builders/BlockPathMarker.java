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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.utils.Utils;

public class BlockPathMarker extends BlockMarker {

	private IIcon activeMarker;

    public BlockPathMarker() {
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePathMarker();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	@SuppressWarnings({ "all" })
	// @Override (client only)
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TilePathMarker marker = (TilePathMarker) iblockaccess.getTileEntity(i, j, k);

		if (l == 1 || (marker != null && marker.tryingToConnect)) {
			return activeMarker;
		} else {
			return super.getIcon(iblockaccess, i, j, k, l);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    blockIcon = par1IconRegister.registerIcon("buildcraft:blockPathMarker");
	    activeMarker = par1IconRegister.registerIcon("buildcraft:blockPathMarkerActive");
	}
}
