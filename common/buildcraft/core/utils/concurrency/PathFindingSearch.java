/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.World;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IZone;
import buildcraft.core.utils.IBlockFilter;

public class PathFindingSearch implements IIterableAlgorithm {

	public static int PATH_ITERATIONS = 1000;

	private static HashMap<Integer, HashSet<BlockIndex>> reservations = new HashMap<Integer, HashSet<BlockIndex>>();

	private World world;
	private BlockIndex start;
	private List<PathFinding> pathFinders;
	private IBlockFilter pathFound;
	private IZone zone;
	private float maxDistance;

	private int searchRadius;
	private int searchX;
	private int searchY;
	private int searchZ;
	private int searchHeight;


	public PathFindingSearch(World iWorld, BlockIndex iStart, IBlockFilter iPathFound, float iMaxDistance, IZone iZone) {
		world = iWorld;
		start = iStart;
		pathFound = iPathFound;

		maxDistance = iMaxDistance;

		pathFinders = new LinkedList<PathFinding>();
		searchRadius = 1;
		searchX = -1;
		searchY = -1;
		searchZ = -1;
		getSearchHeight(start.x + searchX, start.z + searchZ);
	}

	@Override
	public void iterate() {
		if (pathFinders.size() < 5 && searchRadius < 64) {
			iterateSearch(PATH_ITERATIONS * 50);
		}
		iteratePathFind(PATH_ITERATIONS);
	}

	private void iterateSearch(int itNumber) {
		for (int i = 0; i < itNumber; ++i) {
			int currX = start.x + searchX;
			int currY = start.y + searchY;
			int currZ = start.z + searchZ;
			if (0 <= currY && currY <= searchHeight) {
				if (isTarget(currX, currY, currZ)) {
					pathFinders.add(new PathFinding(world, start, new BlockIndex(currX, currY, currZ), 0, maxDistance));
				}
			}

			nextSearchStep();

			if (pathFinders.size() >= 5) {
				return;
			}
		}
	}

	private void nextSearchStep() {
		// Step through each block in a hollow cube of size (searchRadius * 2 -1), if done
		// add 1 to the radius and start over.

		// Step to the next Y
		if (Math.abs(searchX) == searchRadius || Math.abs(searchZ) == searchRadius) {
			searchY += 1;
		} else {
			searchY += searchRadius * 2;
		}

		if (searchY > searchRadius) {
			// Step to the next Z
			searchY = -searchRadius;
			searchZ += 1;

			if (searchZ > searchRadius) {
				// Step to the next X
				searchZ = -searchRadius;
				searchX += 1;

				if (searchX > searchRadius) {
					// Step to the next radius
					searchRadius += 1;
					searchX = -searchRadius;
					searchY = -searchRadius;
					searchZ = -searchRadius;
				}
			}
			searchHeight = getSearchHeight(start.x + searchX, start.z + searchZ);
		}
	}

	private boolean isTarget(int x, int y, int z) {
		if (zone != null && !zone.contains(x, y, z)) {
			return false;
		}
		if (!pathFound.matches(world, x, y, z)) {
			return false;
		}
		synchronized (reservations) {
			if (reservations.containsKey(world.provider.dimensionId)) {
				HashSet<BlockIndex> dimReservations = reservations
						.get(world.provider.dimensionId);
				if (dimReservations.contains(new BlockIndex(x, y, z))) {
					return false;
				}
			}
		}
		if (!BuildCraftAPI.isSoftBlock(world, x - 1, y, z)
				&& !BuildCraftAPI.isSoftBlock(world, x + 1, y, z)
				&& !BuildCraftAPI.isSoftBlock(world, x, y, z - 1)
				&& !BuildCraftAPI.isSoftBlock(world, x, y, z + 1)
				&& !BuildCraftAPI.isSoftBlock(world, x, y - 1, z)
				&& !BuildCraftAPI.isSoftBlock(world, x, y + 1, z)) {
			return false;
		}
		return true;
	}

	private int getSearchHeight(int x, int z) {
		if (world.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
			return 256;
		} else {
			return -1;
		}
	}

	public void iteratePathFind(int itNumber) {
		for (PathFinding pathFinding : new ArrayList<PathFinding>(pathFinders)) {
			pathFinding.iterate(itNumber / pathFinders.size());
			if (pathFinding.isDone()) {
				LinkedList<BlockIndex> path = pathFinding.getResult();
				if (path != null && path.size() > 0) {
					if (reserve(path.getLast())) {
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
		return searchRadius >= 64;
	}

	public LinkedList<BlockIndex> getResult() {
		for (PathFinding pathFinding : pathFinders) {
			if (pathFinding.isDone()) {
				return pathFinding.getResult();
			}
		}
		return new LinkedList<BlockIndex>();
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
