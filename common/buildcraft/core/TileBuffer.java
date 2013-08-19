/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.api.core.SafeTimeTracker;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public final class TileBuffer {

	private int blockID = 0;
	private TileEntity tile;
	private final SafeTimeTracker tracker = new SafeTimeTracker();
	private final World world;
	final int x, y, z;
	private final boolean loadUnloaded;

	public TileBuffer(World world, int x, int y, int z, boolean loadUnloaded) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.loadUnloaded = loadUnloaded;

		refresh();
	}

	public final void refresh() {
		tile = null;
		blockID = 0;
		if (!loadUnloaded && !world.blockExists(x, y, z)) {
			return;
		}
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

	public boolean exists() {
		if(tile != null && !tile.isInvalid())
			return true;
		return world.blockExists(x, y, z);
	}

	public static TileBuffer[] makeBuffer(World world, int x, int y, int z, boolean loadUnloaded) {
		TileBuffer[] buffer = new TileBuffer[6];
		for (int i = 0; i < 6; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			buffer[i] = new TileBuffer(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, loadUnloaded);
		}
		return buffer;
	}
}
