/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.BlockBuildCraft;

public class BlockEnergyReceiver extends BlockBuildCraft {

	public BlockEnergyReceiver() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int p) {
		return new TileEnergyReceiver();

	}

	/*@Override
	public boolean isOpaqueCube() {
		return false;
	}


	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}


	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}*/

}
