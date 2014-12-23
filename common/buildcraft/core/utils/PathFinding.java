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

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IZone;
import buildcraft.core.robots.IBlockFilter;

/**
 * This class implements a 3D path finding based on the A* algorithm, following
 * guidelines documented on http://www.policyalmanac.org/games/aStarTutorial.htm
 * .
 */
public class PathFinding {

	public static int PATH_ITERATIONS = 1000;

	private World world;
	private BlockPos start;
	private BlockPos end;
	private BlockPos boxEnd;
	private IBlockFilter pathFound;
	private float maxDistance = -1;
	private float sqrMaxDistance = -1;
	private IZone zone;
	private double maxDistanceToEnd = 0;

	private HashMap<BlockPos, Node> openList = new HashMap<BlockPos, PathFinding.Node>();
	private HashMap<BlockPos, Node> closedList = new HashMap<BlockPos, PathFinding.Node>();

	private Node nextIteration;

	private LinkedList<BlockPos> result;

	private boolean endReached = false;

	public PathFinding(World iWorld, BlockPos iStart, BlockPos iEnd) {
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

	public PathFinding(World iWorld, BlockPos iStart, BlockPos iEnd, double iMaxDistanceToEnd) {
		this(iWorld, iStart, iEnd);

		maxDistanceToEnd = iMaxDistanceToEnd;
	}

	// TODO: It's probably more efficient to start a search first, and then to
	// compute the path, instead of computing all possible path from the get
	// go.
	public PathFinding(World iWorld, BlockPos iStart, IBlockFilter iPathFound, float iMaxDistance, IZone iZone) {
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
		maxDistance = iMaxDistance;
		sqrMaxDistance = maxDistance * maxDistance;
		zone = iZone;
	}

	public void iterate() {
		iterate(PATH_ITERATIONS);
	}

	public void iterate(int itNumber) {
		for (int i = 0; i < itNumber; ++i) {
			if (nextIteration == null) {
				return;
			}

			if (endReached) {
				result = new LinkedList<BlockPos>();

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

	public LinkedList<BlockPos> getResult() {
		if (result != null) {
			return result;
		} else {
			return new LinkedList<BlockPos>();
		}
	}

	private Node iterate(Node from) {
		openList.remove(from.index);
		closedList.put(from.index, from);

		ArrayList<Node> nodes = new ArrayList<Node>();
		byte[][][] resultMoves = movements(from);

		for (int dx = -1; dx <= +1; ++dx) {
			for (int dy = -1; dy <= +1; ++dy) {
				for (int dz = -1; dz <= +1; ++dz) {
					if (resultMoves[dx + 1][dy + 1][dz + 1] == 0) {
						continue;
					}

					int x = from.index.getX() + dx;
					int y = from.index.getY() + dy;
					int z = from.index.getZ() + dz;

					Node nextNode = new Node();
					nextNode.parent = from;
					nextNode.index = new BlockPos(x, y, z);

					if (resultMoves[dx + 1][dy + 1][dz + 1] == 2) {
						endReached = true;
						return nextNode;
					}

					nextNode.movementCost = from.movementCost + distance(nextNode.index, from.index);

					if (end != null) {
						nextNode.destinationCost = distance(nextNode.index, end);
					} else if (zone != null) {
						if (zone.contains(x, y, z)) {
							nextNode.destinationCost = 0;
						} else {
							nextNode.destinationCost = zone.distanceTo(nextNode.index);
						}
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
		public BlockPos index;
	}

	private static double distance(BlockPos i1, BlockPos i2) {
		return Math.sqrt(i1.distanceSq(i2));
	}

	private boolean endReached(int x, int y, int z) {
		if (zone != null && !zone.contains(x, y, z)) {
			return false;
		} else if (pathFound != null) {
			return pathFound.matches(world, new BlockPos(x, y, z));
		} else {
			if (maxDistanceToEnd == 0) {
				return end.getX() == x && end.getY() == y && end.getZ() == z;
			} else {
				return BuildCraftAPI.isSoftBlock(world, new BlockPos(x, y, z))
						&& distance(new BlockPos(x, y, z), end) <= maxDistanceToEnd;
			}
		}
	}

	private byte[][][] movements(Node from) {
		byte[][][] resultMoves = new byte[3][3][3];

		for (int dx = -1; dx <= +1; ++dx) {
			for (int dy = -1; dy <= +1; ++dy) {
				for (int dz = -1; dz <= +1; ++dz) {
					int x = from.index.getX() + dx;
					int y = from.index.getY() + dy;
					int z = from.index.getZ() + dz;

					if (endReached(x, y, z)) {
						resultMoves[dx + 1][dy + 1][dz + 1] = 2;
					} else if (!BuildCraftAPI.isSoftBlock(world, new BlockPos(x, y, z))) {
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


		if (maxDistance != -1) {
			for (int dx = -1; dx <= +1; ++dx) {
				for (int dy = -1; dy <= +1; ++dy) {
					for (int dz = -1; dz <= +1; ++dz) {
						int x = from.index.getX() + dx;
						int y = from.index.getY() + dy;
						int z = from.index.getZ() + dz;

						float distX = x - start.getX();
						float distY = y - start.getY();
						float distZ = z - start.getZ();
						float sqrDist = distX * distX + distY * distY + distZ * distZ;

						if (sqrDist > sqrMaxDistance) {
							resultMoves[dx + 1][dy + 1][dz + 1] = 0;
						}
					}
				}
			}
		}

		return resultMoves;
	}

}
