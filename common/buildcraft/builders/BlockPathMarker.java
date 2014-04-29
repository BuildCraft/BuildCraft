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
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPathMarker extends BlockMarker {

	private IIcon activeMarker;

    public BlockPathMarker() {
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePathMarker();
	}

	@Override
	@SuppressWarnings({ "all" })
	// @Override (client only)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int meta) {
		TileEntity tile = access.getTileEntity(x, y, z);
		if (tile instanceof  TilePathMarker) {
			TilePathMarker marker = ((TilePathMarker) tile);
			if (meta == 1 || (marker != null && marker.tryingToConnect)) {
				return activeMarker;
			}
		}
		return super.getIcon(access, x, y, z, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
	    blockIcon = par1IconRegister.registerIcon("buildcraft:blockPathMarker");
	    activeMarker = par1IconRegister.registerIcon("buildcraft:blockPathMarkerActive");
	}
}
