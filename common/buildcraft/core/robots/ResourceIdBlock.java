/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

import buildcraft.api.robots.ResourceId;

public class ResourceIdBlock extends ResourceId {

	public ResourceIdBlock() {

	}

	public ResourceIdBlock(int x, int y, int z) {
		index = new BlockPos(x, y, z);
	}

	public ResourceIdBlock(BlockPos iIndex) {
		index = iIndex;
	}

	public ResourceIdBlock(TileEntity tile) {
		index = tile.getPos();
	}

}
