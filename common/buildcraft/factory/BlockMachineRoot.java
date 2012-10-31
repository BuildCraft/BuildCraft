/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.core.IMachine;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public abstract class BlockMachineRoot extends BlockContainer {

	protected BlockMachineRoot(int i, Material material) {
		super(i, material);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k) {
		for (int x = i - 1; x <= i + 1; ++x)
			for (int y = j - 1; y <= j + 1; ++y)
				for (int z = k - 1; z <= k + 1; ++z) {
					TileEntity tile = iblockaccess.getBlockTileEntity(x, y, z);

					if (tile instanceof IMachine && ((IMachine) tile).isActive())
						return super.getBlockBrightness(iblockaccess, i, j, k) + 0.5F;
				}

		return super.getBlockBrightness(iblockaccess, i, j, k);
	}

}
