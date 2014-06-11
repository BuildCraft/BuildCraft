/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.world.World;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.BlockIndex;

/**
 * This class implements a 3D path finding based on the A* algorithm, following
 * guidelines documented on http://www.policyalmanac.org/games/aStarTutorial.htm
 * .
 */
public class PathFinding {

	public static int PATH_ITERATIONS = 1000;

	private World world;
	private BlockIndex start;
	private BlockIndex end;
	private IPathFound pathFound;

	private HashMap<BlockIndex, Node> openList = new HashMap<BlockIndex, PathFinding.Node>();
	private HashMap<BlockIndex, Node> closedList = new HashMap<BlockIndex, PathFinding.Node>();

	private Node nextIteration;

	private LinkedList<BlockIndex> result;

	public PathFinding(World iWorld, BlockIndex iStart, BlockIndex iEnd) {
		world = iWorld;
		start = iStart;
		end = iEnd;

		Node startNode = new Node();
		startNode.parent = null;
		startNode.movementCost = 0;
		startNode.destinationCost = distance(start, end);
		startNode.totalWeight = startNode.movementCost + startNode.destinationCost;
		startNode.index = iStart;
		openList.put(start, startNode);
		nextIteration = startNode;
	}

	public PathFinding(World iWorld, BlockIndex iStart, IPathFound iPathFound) {
		world = iWorld;
		start = iStart;
		pathFound = iPathFound;

		Node startNode = new Node();
		startNode.parent = null;
		startNode.movementCost = 0;
		startNode.destinationCost = 0;
		startNode.totalWeight = startNode.movementCost + startNode.destinationCost;
		startNode.index = iStart;
		openList.put(start, startNode);
		nextIteration = startNode;
	}

	public void iterate() {
		iterate(PATH_ITERATIONS);
	}

	public void iterate(int itNumber) {
		if (nextIteration == null) {
			return;
		}

		for (int i = 0; i < itNumber; ++i) {
			if (endReached(nextIteration.index.x, nextIteration.index.y, nextIteration.index.z)) {
				result = new LinkedList<BlockIndex>();

				while (nextIteration != null) {
					result.addFirst(nextIteration.index);
					nextIteration = nextIteration.parent;
				}

				return;
			} else {
				nextIteration = iterate(nextIteration);
			}
		}
	}

	public boolean isDone() {
		return nextIteration == null;
	}

	public LinkedList<BlockIndex> getResult() {
		if (result != null) {
			return result;
		} else {
			return new LinkedList<BlockIndex>();
		}
	}

	private Node iterate(Node from) {
		openList.remove(from.index);
		closedList.put(from.index, from);

		ArrayList nodes = new ArrayList<Node>();
		byte[][][] resultMoves = movements(from);

		for (int dx = -1; dx <= +1; ++dx) {
			for (int dy = -1; dy <= +1; ++dy) {
				for (int dz = -1; dz <= +1; ++dz) {
					if (resultMoves[dx + 1][dy + 1][dz + 1] == 0) {
						continue;
					}

					int x = from.index.x + dx;
					int y = from.index.y + dy;
					int z = from.index.z + dz;

					Node nextNode = new Node();
					nextNode.parent = from;
					nextNode.index = new BlockIndex(x, y, z);

					if (resultMoves[dx + 1][dy + 1][dz + 1] == 2) {
						return nextNode;
					}

					nextNode.movementCost = from.movementCost + distance(nextNode.index, from.index);

					if (end != null) {
						nextNode.destinationCost = distance(nextNode.index, end);
					} else {
						nextNode.destinationCost = 0;
					}

					nextNode.totalWeight = nextNode.movementCost + nextNode.destinationCost;

					if (closedList.containsKey(nextNode.index)) {
						continue;
					} else if (openList.containsKey(nextNode.index)) {
						Node tentative = openList.get(nextNode.index);

						if (tentative.movementCost < nextNode.movementCost) {
							nextNode = tentative;
						} else {
							openList.put(nextNode.index, nextNode);
						}
					} else {
						openList.put(nextNode.index, nextNode);
					}

					nodes.add(nextNode);
				}
			}
		}

		nodes.addAll(openList.values());

		return findSmallerWeight(nodes);
	}

	private Node findSmallerWeight(Collection<Node> collection) {
		Node found = null;

		for (Node n : collection) {
			if (found == null) {
				found = n;
			} else if (n.totalWeight < found.totalWeight) {
				found = n;
			}
		}

		return found;
	}

	private static class Node {
		public Node parent;
		public double movementCost;
		public double destinationCost;
		public double totalWeight;
		public BlockIndex index;
	}

	private static double distance(BlockIndex i1, BlockIndex i2) {
		double dx = (double) i1.x - (double) i2.x;
		double dy = (double) i1.y - (double) i2.y;
		double dz = (double) i1.z - (double) i2.z;

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private boolean endReached(int x, int y, int z) {
		if (pathFound != null) {
			return pathFound.endReached(world, x, y, z);
		} else {
			return end.x == x && end.y == y && end.z == z;
		}
	}

	private byte[][][] movements(Node from) {
		byte[][][] resultMoves = new byte[3][3][3];

		for (int dx = -1; dx <= +1; ++dx) {
			for (int dy = -1; dy <= +1; ++dy) {
				for (int dz = -1; dz <= +1; ++dz) {
					int x = from.index.x + dx;
					int y = from.index.y + dy;
					int z = from.index.z + dz;

					if (endReached(x, y, z)) {
						resultMoves[dx + 1][dy + 1][dz + 1] = 2;
					} else if (!BuildCraftAPI.isSoftBlock(world, x, y, z)) {
						resultMoves[dx + 1][dy + 1][dz + 1] = 0;
					} else {
						resultMoves[dx + 1][dy + 1][dz + 1] = 1;
					}
				}
			}
		}

		resultMoves[1][1][1] = 0;

		if (resultMoves[0][1][1] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[0][i][j] = 0;
				}
			}
		}

		if (resultMoves[2][1][1] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[2][i][j] = 0;
				}
			}
		}

		if (resultMoves[1][0][1] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[i][0][j] = 0;
				}
			}
		}

		if (resultMoves[1][2][1] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[i][2][j] = 0;
				}
			}
		}

		if (resultMoves[1][1][0] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[i][j][0] = 0;
				}
			}
		}

		if (resultMoves[1][1][2] == 0) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j <= 2; ++j) {
					resultMoves[i][j][2] = 0;
				}
			}
		}

		if (resultMoves[0][0][1] == 0) {
			resultMoves[0][0][0] = 0;
			resultMoves[0][0][2] = 0;
		}

		if (resultMoves[0][2][1] == 0) {
			resultMoves[0][2][0] = 0;
			resultMoves[0][2][2] = 0;
		}

		if (resultMoves[2][0][1] == 0) {
			resultMoves[2][0][0] = 0;
			resultMoves[2][0][2] = 0;
		}

		if (resultMoves[2][2][1] == 0) {
			resultMoves[2][2][0] = 0;
			resultMoves[2][2][2] = 0;
		}

		if (resultMoves[0][1][0] == 0) {
			resultMoves[0][0][0] = 0;
			resultMoves[0][2][0] = 0;
		}

		if (resultMoves[0][1][2] == 0) {
			resultMoves[0][0][2] = 0;
			resultMoves[0][2][2] = 0;
		}

		if (resultMoves[2][1][0] == 0) {
			resultMoves[2][0][0] = 0;
			resultMoves[2][2][0] = 0;
		}

		if (resultMoves[2][1][2] == 0) {
			resultMoves[2][0][2] = 0;
			resultMoves[2][2][2] = 0;
		}

		if (resultMoves[1][0][0] == 0) {
			resultMoves[0][0][0] = 0;
			resultMoves[2][0][0] = 0;
		}

		if (resultMoves[1][0][2] == 0) {
			resultMoves[0][0][2] = 0;
			resultMoves[2][0][2] = 0;
		}

		if (resultMoves[1][2][0] == 0) {
			resultMoves[0][2][0] = 0;
			resultMoves[2][2][0] = 0;
		}

		if (resultMoves[1][2][2] == 0) {
			resultMoves[0][2][2] = 0;
			resultMoves[2][2][2] = 0;
		}

		return resultMoves;
	}

}
