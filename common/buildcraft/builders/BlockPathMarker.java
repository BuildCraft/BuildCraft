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
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.utils.Utils;

public class BlockPathMarker extends BlockMarker {

	//private IIcon activeMarker;

    public BlockPathMarker() {
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePathMarker();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);
	}

	/*@Override
	@SuppressWarnings({ "all" })
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
	}*/
}
