/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.core.utils.Utils;

public class BlockPathMarker extends BlockMarker {

	public BlockPathMarker(int i) {
		super(i);

		blockIndexInTexture = 3 * 16 + 10;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TilePathMarker();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TilePathMarker marker = (TilePathMarker) iblockaccess.getBlockTileEntity(i, j, k);

		if (l == 1 || (marker != null && marker.tryingToConnect))
			return 3 * 16 + 11;
		else
			return 3 * 16 + 10;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
