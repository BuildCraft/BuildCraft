/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.world.IBlockAccess;

import buildcraft.core.BlockIndex;

/**
 * This class implements a 3D path finding based on the A* algorithm, following
 * guidelines documented on
 * http://www.policyalmanac.org/games/aStarTutorial.htm.
 */
public class PathFinding {

	private IBlockAccess world;
	private BlockIndex start;
	private BlockIndex end;

	private HashMap<BlockIndex, Node> openList = new HashMap<BlockIndex, PathFinding.Node>();
	private HashMap<BlockIndex, Node> closedList = new HashMap<BlockIndex, PathFinding.Node>();

	public PathFinding(IBlockAccess iWorld, BlockIndex iStart, BlockIndex iEnd) {
		world = iWorld;
		start = iStart;
		end = iEnd;
	}

	public void iterate(int itNumber) {

	}

	public void iterate(BlockIndex from) {
		for (int x = from.x - 1; x <= from.x + 1; ++x) {
			for (int y = from.y - 1; y <= from.y + 1; ++y) {
				for (int z = from.z - 1; y <= from.z + 1; ++z) {
					if (x == from.x && y == from.y && z == from.z) {
						continue;
					}

				}
			}
		}
	}

	public LinkedList<BlockIndex> getPath() {
		return null;
	}

	private static class Node {
		public Node parent;
		public double movementCost;
		public double destinationCost;
		public double totalWeight;
	}

}
