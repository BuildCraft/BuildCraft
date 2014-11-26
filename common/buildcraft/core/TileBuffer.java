/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.SafeTimeTracker;

public final class TileBuffer {

	private IBlockState state = null;
	private TileEntity tile;

	private final SafeTimeTracker tracker = new SafeTimeTracker(20, 5);
	private final World world;
	private final BlockPos pos;
	private final boolean loadUnloaded;

	public TileBuffer(World world, BlockPos pos, boolean loadUnloaded) {
		this.world = world;
		this.pos = pos;
		this.loadUnloaded = loadUnloaded;

		refresh();
	}

	public void refresh() {
		tile = null;
		state = null;

		if (!loadUnloaded && !world.isBlockLoaded(pos, true)) {
			return;
		}

		state = world.getBlockState(pos);

		if (state != null && state.getBlock().hasTileEntity(state)) {
			tile = world.getTileEntity(pos);
		}
	}

	public void set(IBlockState state, TileEntity tile) {
		this.state = state;
		this.tile = tile;
		tracker.markTime(world);
	}


	public IBlockState getBlockState() {
		if ((tile != null && tile.isInvalid()) || (tile == null && tracker.markTimeIfDelay(world))) {
			refresh();
		}

		return state;
	}

	public Block getBlock() {
		return getBlockState().getBlock();
	}

	public TileEntity getTile() {
		if (tile != null && !tile.isInvalid()) {
			return tile;
		}

		if (tracker.markTimeIfDelay(world)) {
			refresh();

			if (tile != null && !tile.isInvalid()) {
				return tile;
			}
		}

		return null;
	}

	public boolean exists() {
		if (tile != null && !tile.isInvalid()) {
			return true;
		}

		return world.isBlockLoaded(pos, true);
	}

	public static TileBuffer[] makeBuffer(World world, BlockPos pos, boolean loadUnloaded) {
		TileBuffer[] buffer = new TileBuffer[6];

		for (EnumFacing d : EnumFacing.values()) {
			buffer[d.ordinal()] = new TileBuffer(world, pos.offset(d), loadUnloaded);
		}

		return buffer;
	}
}
