/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.api.core.SafeTimeTracker;
import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

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

		if (Block.blocksList[blockID] instanceof BlockContainer)
			tile = world.getBlockTileEntity(this.x, this.y, this.z);
	}

	public void set(int blockID, TileEntity tile) {
		this.blockID = blockID;
		this.tile = tile;
		tracker.markTime(world);
	}

	public int getBlockID() {
		if (tile != null && !tile.isInvalid())
			return blockID;
		else {
			if (tracker.markTimeIfDelay(world, 20))
				refresh();

			return blockID;
		}
	}

	public TileEntity getTile() {
		if (tile != null && !tile.isInvalid())
			return tile;
		else {
			if (tracker.markTimeIfDelay(world, 20))
				refresh();

			return tile;
		}
	}

}
