/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.pathfinding;

import buildcraft.api.core.IBlockFilter;
import buildcraft.api.core.IZone;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.IIterableAlgorithm;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

// Calen 1.18.2 from 1.8.9
public class PathFindingSearch implements IIterableAlgorithm {

    public static final int PATH_ITERATIONS = 1000;

    // private static final HashMap<Integer, HashSet<BlockPos>> reservations = new HashMap<Integer, HashSet<BlockPos>>();
    private static final HashMap<ResourceKey<Level>, HashSet<BlockPos>> reservations = new HashMap<ResourceKey<Level>, HashSet<BlockPos>>();

    private Level world;
    private BlockPos start;
    private List<PathFinding> pathFinders;
    private IBlockFilter pathFound;
    private IZone zone;
    private float maxDistance;
    private Iterator<BlockPos> blockIter;

    private double maxDistanceToEnd;

    public PathFindingSearch(Level iWorld, BlockPos iStart, Iterator<BlockPos> iBlockIter, IBlockFilter iPathFound, double iMaxDistanceToEnd,
            float iMaxDistance, IZone iZone) {
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

            BlockPos delta = blockIter.next();
            BlockPos block = new BlockPos(start.getX() + delta.getX(), ((start.getY() + delta.getY()) > 0) ? start.getY() + delta.getY() : 0, start
                    .getZ() + delta.getZ());
            if (isLoadedChunk(block.getX(), block.getZ())) {
                if (isTarget(block)) {
                    pathFinders.add(new PathFinding(world, start, block, maxDistanceToEnd, maxDistance));
                }
            }

            if (pathFinders.size() >= 5) {
                return;
            }
        }
    }

    private boolean isTarget(BlockPos block) {
        if (zone != null && !zone.contains(VecUtil.convert(block))) {
            return false;
        }
        if (!pathFound.matches(world, block)) {
            return false;
        }
        synchronized (reservations) {
            // if (reservations.containsKey(world.provider.getDimensionId()))
            if (reservations.containsKey(world.dimension())) {
                // HashSet<BlockPos> dimReservations = reservations.get(world.provider.getDimensionId());
                HashSet<BlockPos> dimReservations = reservations.get(world.dimension());
                if (dimReservations.contains(block)) {
                    return false;
                }
            }
        }
        if (!BlockUtil.isSoftBlock(world, block.west()) &&
                !BlockUtil.isSoftBlock(world, block.east()) &&
                !BlockUtil.isSoftBlock(world, block.north()) &&
                !BlockUtil.isSoftBlock(world, block.south()) &&
                !BlockUtil.isSoftBlock(world, block.below()) &&
                !BlockUtil.isSoftBlock(world, block.above())) {
            return false;
        }
        return true;
    }

    private boolean isLoadedChunk(int x, int z) {
        // return world.getChunkProvider().chunkExists(x >> 4, z >> 4);
        return world.getChunkSource().hasChunk(x >> 4, z >> 4);
    }

    public void iteratePathFind(int itNumber) {
        for (PathFinding pathFinding : new ArrayList<PathFinding>(pathFinders)) {
            pathFinding.iterate(itNumber / pathFinders.size());
            if (pathFinding.isDone()) {
                LinkedList<BlockPos> path = pathFinding.getResult();
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

    public LinkedList<BlockPos> getResult() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.getResult();
            }
        }
        return new LinkedList<BlockPos>();
    }

    public BlockPos getResultTarget() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.end();
            }
        }
        return null;
    }

    private boolean reserve(BlockPos block) {
        synchronized (reservations) {
            // if (!reservations.containsKey(world.provider.getDimensionId()))
            if (!reservations.containsKey(world.dimension())) {
                // reservations.put(world.provider.getDimensionId(), new HashSet<BlockPos>());
                reservations.put(world.dimension(), new HashSet<BlockPos>());
            }
            // HashSet<BlockPos> dimReservations = reservations.get(world.provider.getDimensionId());
            HashSet<BlockPos> dimReservations = reservations.get(world.dimension());
            if (dimReservations.contains(block)) {
                return false;
            }
            dimReservations.add(block);
            return true;
        }
    }

    public void unreserve(BlockPos block) {
        synchronized (reservations) {
            // if (reservations.containsKey(world.provider.getDimensionId()))
            if (reservations.containsKey(world.dimension())) {
                // reservations.get(world.provider.getDimensionId()).remove(block);
                reservations.get(world.dimension()).remove(block);
            }
        }
    }
}
