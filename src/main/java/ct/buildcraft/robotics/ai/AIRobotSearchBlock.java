package ct.buildcraft.robotics.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Searches for a matching work block and returns a path to a soft block adjacent to it.
 *
 * <p>The first robotics port used a pure reachable-air BFS and checked only blocks adjacent to every visited air node.
 * In open areas that meant the search budget was spent on the volume of air around the robot before it ever reached
 * trees a few chunks away. The original BuildCraft logic did the opposite: scan candidate blocks in an expanding area,
 * then pathfind only to real targets. This implementation keeps that behavior while using the modern soft-block checks.</p>
 */
public class AIRobotSearchBlock extends AIRobot {
    private static final int DEFAULT_RANGE = 96;
    private static final int SEARCHES_PER_TICK = 65536;
    private static final int MAX_ASTAR_VISITED = 262144;

    public BlockIndex blockFound;
    public LinkedList<BlockIndex> path;

    private IBlockFilter filter;
    private boolean random;
    private double maxDistanceToEnd;
    private IZone zone;
    private boolean searched;
    private BlockPos start;
    private int searchRange;
    private CandidateScanner scanner;

    public AIRobotSearchBlock(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotSearchBlock(EntityRobotBase robot, boolean random, IBlockFilter filter, double maxDistanceToEnd) {
        this(robot);
        this.random = random;
        this.filter = filter;
        this.maxDistanceToEnd = maxDistanceToEnd;
        this.zone = robot.getZoneToWork();
    }

    @Override
    public void update() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (!searched) {
            beginSearch();
        }

        SearchResult result = continueSearch();
        if (result != null) {
            blockFound = new BlockIndex(result.target());
            path = result.path();
            terminate();
        } else if (scanner == null || scanner.isDone()) {
            setSuccess(false);
            terminate();
        }
    }

    private void beginSearch() {
        searched = true;
        start = robot.blockPosition();
        searchRange = DEFAULT_RANGE;
        if (maxDistanceToEnd > 0) {
            searchRange = Math.max(searchRange, (int) Math.ceil(maxDistanceToEnd) + 8);
        }
        scanner = new CandidateScanner(robot.level, start, searchRange, random);
    }

    private SearchResult continueSearch() {
        int processed = 0;
        while (scanner != null && !scanner.isDone() && processed++ < SEARCHES_PER_TICK) {
            BlockPos candidate = scanner.next();
            if (candidate == null) {
                break;
            }

            SearchResult result = evaluateCandidate(candidate);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private SearchResult evaluateCandidate(BlockPos target) {
        Level level = robot.level;
        if (!level.isLoaded(target)) {
            return null;
        }
        if (zone != null && !zone.contains(center(target))) {
            return null;
        }
        if (!filter.matches(level, target)) {
            return null;
        }

        List<BlockPos> adjacentPositions = adjacentSoftTargets(level, target);
        if (adjacentPositions.isEmpty()) {
            return null;
        }
        if (random) {
            Collections.shuffle(adjacentPositions);
        }

        LinkedList<BlockIndex> bestPath = null;
        for (BlockPos adjacent : adjacentPositions) {
            LinkedList<BlockIndex> candidatePath = pathToAdjacent(level, adjacent);
            if (candidatePath != null && (bestPath == null || candidatePath.size() < bestPath.size())) {
                bestPath = candidatePath;
            }
        }
        return bestPath == null ? null : new SearchResult(target, bestPath);
    }

    private List<BlockPos> adjacentSoftTargets(Level level, BlockPos target) {
        List<BlockPos> result = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = target.relative(direction);
            // Match the original BuildCraft behaviour: the work zone constrains the target block, not the
            // navigation corridor. A station/robot may legitimately start outside the zone and fly through
            // non-zone air to reach a block inside it, and edge blocks may only have a reachable soft side just
            // outside the zone.
            if (isSoft(level, adjacent)) {
                result.add(adjacent);
            }
        }
        return result;
    }

    private LinkedList<BlockIndex> pathToAdjacent(Level level, BlockPos target) {
        if (start.equals(target)) {
            return new LinkedList<>();
        }

        LinkedList<BlockIndex> direct = directFallback(start, target, level);
        if (direct != null) {
            return direct;
        }
        return findAStarPath(start, target, level);
    }

    private LinkedList<BlockIndex> findAStarPath(BlockPos start, BlockPos target, Level level) {
        int hardLimit = Math.max(searchRange + 16, (int) Math.ceil(Math.sqrt(distanceSqr(start, target))) + 8);
        int hardLimitSqr = hardLimit * hardLimit;
        PriorityQueue<PathNode> open = new PriorityQueue<>((a, b) -> {
            int score = Double.compare(a.score(), b.score());
            return score != 0 ? score : Long.compare(a.sequence(), b.sequence());
        });
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        Map<BlockPos, Integer> bestCost = new HashMap<>();
        Set<BlockPos> closed = new HashSet<>();
        long sequence = 0;

        bestCost.put(start, 0);
        open.add(new PathNode(start, heuristic(start, target), sequence++));

        while (!open.isEmpty() && closed.size() < MAX_ASTAR_VISITED) {
            PathNode node = open.poll();
            BlockPos current = node.pos();
            if (!closed.add(current)) {
                continue;
            }
            if (current.equals(target)) {
                return reconstruct(start, target, parent);
            }

            int currentCost = bestCost.getOrDefault(current, Integer.MAX_VALUE);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (closed.contains(next)) continue;
                if (distanceSqr(next, start) > hardLimitSqr) continue;
                // Do not clamp the path itself to the work zone. Only the searched target block is zone-limited.
                if (!isSoft(level, next)) continue;

                int newCost = currentCost + 1;
                if (newCost < bestCost.getOrDefault(next, Integer.MAX_VALUE)) {
                    bestCost.put(next, newCost);
                    parent.put(next, current);
                    open.add(new PathNode(next, newCost + heuristic(next, target), sequence++));
                }
            }
        }
        return null;
    }

    private static LinkedList<BlockIndex> directFallback(BlockPos start, BlockPos target, Level level) {
        int[][] orders = {
                {0, 1, 2}, {0, 2, 1},
                {1, 0, 2}, {1, 2, 0},
                {2, 0, 1}, {2, 1, 0}
        };
        for (int[] order : orders) {
            LinkedList<BlockIndex> out = traceAxisPath(start, target, level, order);
            if (out != null) {
                return out;
            }
        }
        return null;
    }

    private static LinkedList<BlockIndex> traceAxisPath(BlockPos start, BlockPos target, Level level, int[] order) {
        LinkedList<BlockIndex> out = new LinkedList<>();
        int[] current = {start.getX(), start.getY(), start.getZ()};
        int[] end = {target.getX(), target.getY(), target.getZ()};

        for (int axis : order) {
            while (current[axis] != end[axis]) {
                current[axis] += Integer.compare(end[axis], current[axis]);
                BlockPos pos = new BlockPos(current[0], current[1], current[2]);
                if (!isSoft(level, pos)) {
                    return null;
                }
                out.add(new BlockIndex(pos));
            }
        }
        return out;
    }

    private static LinkedList<BlockIndex> reconstruct(BlockPos start, BlockPos target, Map<BlockPos, BlockPos> parent) {
        List<BlockPos> reversed = new ArrayList<>();
        BlockPos current = target;
        while (current != null && !current.equals(start)) {
            reversed.add(current);
            current = parent.get(current);
        }
        Collections.reverse(reversed);
        LinkedList<BlockIndex> out = new LinkedList<>();
        for (BlockPos pos : reversed) {
            out.add(new BlockIndex(pos));
        }
        return out;
    }

    private static boolean isSoft(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static double distanceSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("blockFound", tag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
    }

    public boolean takeResource() {
        return blockFound != null && robot.getRegistry() != null
                && robot.getRegistry().take(new ct.buildcraft.api.robots.ResourceIdBlock(blockFound), robot);
    }

    public void unreserve() {
        if (blockFound != null && robot.getRegistry() != null) {
            robot.getRegistry().release(new ct.buildcraft.api.robots.ResourceIdBlock(blockFound));
        }
    }

    @Override
    public int getEnergyCost() {
        return 2;
    }

    private record SearchResult(BlockPos target, LinkedList<BlockIndex> path) {
    }

    private record PathNode(BlockPos pos, double score, long sequence) {
    }

    private static final class CandidateScanner {
        private final Level level;
        private final BlockPos start;
        private final int range;
        private final boolean random;
        private final int minY;
        private final int maxY;
        private int radius;
        private List<BlockPos> currentShell;
        private int shellIndex;
        private boolean done;

        private CandidateScanner(Level level, BlockPos start, int range, boolean random) {
            this.level = level;
            this.start = start;
            this.range = range;
            this.random = random;
            this.minY = level.getMinBuildHeight();
            this.maxY = level.getMaxBuildHeight() - 1;
            this.radius = 0;
            buildShell();
        }

        private boolean isDone() {
            return done;
        }

        private BlockPos next() {
            while (!done) {
                if (currentShell != null && shellIndex < currentShell.size()) {
                    return currentShell.get(shellIndex++);
                }

                radius++;
                if (radius > range) {
                    done = true;
                    return null;
                }
                buildShell();
            }
            return null;
        }

        private void buildShell() {
            currentShell = new ArrayList<>();
            shellIndex = 0;

            if (radius == 0) {
                if (start.getY() >= minY && start.getY() <= maxY) {
                    currentShell.add(start);
                }
                return;
            }

            int sx = start.getX();
            int sy = start.getY();
            int sz = start.getZ();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz)) != radius) {
                            continue;
                        }
                        int y = sy + dy;
                        if (y < minY || y > maxY) {
                            continue;
                        }
                        currentShell.add(new BlockPos(sx + dx, y, sz + dz));
                    }
                }
            }
            if (random) {
                Collections.shuffle(currentShell);
            }
        }
    }
}
