/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.api.core.SafeTimeTracker;

public class TileBuffer {

	private int blockID = 0;
	private TileEntity tile;
	private SafeTimeTracker tracker = new SafeTimeTracker();
	private World world;
	int x, y, z;

	public void initialize(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;

		refresh();
	}

	public void refresh() {
		tile = null;
		blockID = world.getBlockId(this.x, this.y, this.z);

		Block block = Block.blocksList[blockID];
		if (block != null && block.hasTileEntity(world.getBlockMetadata(this.x, this.y, this.z))) {
			tile = world.getBlockTileEntity(this.x, this.y, this.z);
		}
	}

	public void set(int blockID, TileEntity tile) {
		this.blockID = blockID;
		this.tile = tile;
		tracker.markTime(world);
	}

	public int getBlockID() {
		if (tile != null && !tile.isInvalid())
			return blockID;

		if (tracker.markTimeIfDelay(world, 20)) {
			refresh();

			if (tile != null && !tile.isInvalid())
				return blockID;
		}

		return 0;
	}

	public TileEntity getTile() {
		if (tile != null && !tile.isInvalid())
			return tile;

		if (tracker.markTimeIfDelay(world, 20)) {
			refresh();

			if (tile != null && !tile.isInvalid())
				return tile;
		}

		return null;
	}

}
