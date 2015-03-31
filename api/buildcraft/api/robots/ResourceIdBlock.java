/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.BlockIndex;

public class ResourceIdBlock extends ResourceId {

	public ResourceIdBlock() {

	}

	public ResourceIdBlock(int x, int y, int z) {
		index = new BlockIndex(x, y, z);
	}

	public ResourceIdBlock(BlockIndex iIndex) {
		index = iIndex;
	}

	public ResourceIdBlock(TileEntity tile) {
		index = new BlockIndex(tile);
	}

}
