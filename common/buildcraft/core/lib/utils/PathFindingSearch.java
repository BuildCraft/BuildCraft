/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.World;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IZone;

public class PathFindingSearch implements IIterableAlgorithm {

	public static final int PATH_ITERATIONS = 1000;
	private static final HashMap<Integer, HashSet<BlockIndex>> reservations = new HashMap<Integer, HashSet<BlockIndex>>();

	private World world;
	private BlockIndex start;
	private List<PathFinding> pathFinders;
	private IBlockFilter pathFound;
	private IZone zone;
	private float maxDistance;
	private Iterator<BlockIndex> blockIter;

	private double maxDistanceToEnd;

	public PathFindingSearch(World iWorld, BlockIndex iStart,
							 Iterator<BlockIndex> iBlockIter, IBlockFilter iPathFound,
							 double iMaxDistanceToEnd, float iMaxDistance, IZone iZone) {
		world = iWorld;
		start = iStart;
		pathFound = iPathFound;

		maxDistance = iMaxDistance;
		maxDistanceToEnd = iMaxDistanceToEnd;
		zone = iZone;
		blockIter = iBlockIter;

		pathFinders = new LinkedList<PathFinding>();
	}

	@Override
	public void iterate() {
		if (pathFinders.size() < 5 && blockIter.hasNext()) {
			iterateSearch(PATH_ITERATIONS * 10);
		}
		iteratePathFind(PATH_ITERATIONS);
	}

	private void iterateSearch(int itNumber) {
		for (int i = 0; i < itNumber; ++i) {
			if (!blockIter.hasNext()) {
				return;
			}

			BlockIndex delta = blockIter.next();
			BlockIndex block = new BlockIndex(start.x + delta.x,
					((start.y + delta.y) > 0) ? start.y + delta.y : 0,
					start.z + delta.z);
			if (isLoadedChunk(block.x, block.z)) {
				if (isTarget(block)) {
					pathFinders.add(new PathFinding(world, start, block, maxDistanceToEnd, maxDistance));
				}
			}

			if (pathFinders.size() >= 5) {
				return;
			}
		}
	}

	private boolean isTarget(BlockIndex block) {
		if (zone != null && !zone.contains(block.x, block.y, block.z)) {
			return false;
		}
		if (!pathFound.matches(world, block.x, block.y, block.z)) {
			return false;
		}
		synchronized (reservations) {
			if (reservations.containsKey(world.provider.dimensionId)) {
				HashSet<BlockIndex> dimReservations = reservations
						.get(world.provider.dimensionId);
				if (dimReservations.contains(block)) {
					return false;
				}
			}
		}
		if (!BuildCraftAPI.isSoftBlock(world, block.x - 1, block.y, block.z)
				&& !BuildCraftAPI.isSoftBlock(world, block.x + 1, block.y, block.z)
				&& !BuildCraftAPI.isSoftBlock(world, block.x, block.y, block.z - 1)
				&& !BuildCraftAPI.isSoftBlock(world, block.x, block.y, block.z + 1)
				&& !BuildCraftAPI.isSoftBlock(world, block.x, block.y - 1, block.z)
				&& !BuildCraftAPI.isSoftBlock(world, block.x, block.y + 1, block.z)) {
			return false;
		}
		return true;
	}

	private boolean isLoadedChunk(int x, int z) {
		return world.getChunkProvider().chunkExists(x >> 4, z >> 4);
	}

	public void iteratePathFind(int itNumber) {
		for (PathFinding pathFinding : new ArrayList<PathFinding>(pathFinders)) {
			pathFinding.iterate(itNumber / pathFinders.size());
			if (pathFinding.isDone()) {
				LinkedList<BlockIndex> path = pathFinding.getResult();
				if (path != null && path.size() > 0) {
					if (reserve(pathFinding.end())) {
						return;
					}
				}
				pathFinders.remove(pathFinding);
			}
		}
	}

	@Override
	public boolean isDone() {
		for (PathFinding pathFinding : pathFinders) {
			if (pathFinding.isDone()) {
				return true;
			}
		}
		return !blockIter.hasNext();
	}

	public LinkedList<BlockIndex> getResult() {
		for (PathFinding pathFinding : pathFinders) {
			if (pathFinding.isDone()) {
				return pathFinding.getResult();
			}
		}
		return new LinkedList<BlockIndex>();
	}

	public BlockIndex getResultTarget() {
		for (PathFinding pathFinding : pathFinders) {
			if (pathFinding.isDone()) {
				return pathFinding.end();
			}
		}
		return null;
	}

	private boolean reserve(BlockIndex block) {
		synchronized (reservations) {
			if (!reservations.containsKey(world.provider.dimensionId)) {
				reservations.put(world.provider.dimensionId,
						new HashSet<BlockIndex>());
			}
			HashSet<BlockIndex> dimReservations = reservations
					.get(world.provider.dimensionId);
			if (dimReservations.contains(block)) {
				return false;
			}
			dimReservations.add(block);
			return true;
		}
	}

	public void unreserve(BlockIndex block) {
		synchronized (reservations) {
			if (reservations.containsKey(world.provider.dimensionId)) {
				reservations.get(world.provider.dimensionId).remove(block);
			}
		}
	}
}
