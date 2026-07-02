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
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Pathing port for picker-level movement. It follows the old robot rule of travelling through soft/non-colliding blocks. */
public class AIRobotGotoBlock extends AIRobotGoto {
    private LinkedList<BlockIndex> path;
    private double prevDistance = Double.MAX_VALUE;
    private int finalX, finalY, finalZ;
    private double maxDistance;
    private BlockIndex lastBlockInPath;
    private boolean loadedFromNBT;
    private int stuckTicks;

    public AIRobotGotoBlock(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
        this(robot, x, y, z, 0);
    }

    public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double maxDistance) {
        super(robot);
        this.finalX = x;
        this.finalY = y;
        this.finalZ = z;
        this.maxDistance = maxDistance;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockIndex> path) {
        super(robot);
        this.path = path;
        if (!path.isEmpty()) {
            BlockIndex last = path.getLast();
            finalX = last.x;
            finalY = last.y;
            finalZ = last.z;
            setNextInPath();
        }
    }

    @Override
    public void start() {
        robot.undock();
    }

    @Override
    public void update() {
        if (loadedFromNBT) {
            setNextInPath();
            loadedFromNBT = false;
        }
        if (path == null) {
            path = findPath();
            if (path == null || path.isEmpty()) {
                setSuccess(false);
                terminate();
                return;
            }
            lastBlockInPath = path.getLast();
            setNextInPath();
        }

        if (path != null && !path.isEmpty()) {
            double distance = distanceTo(nextX, nextY, nextZ);
            boolean notProgressing = distance > prevDistance + 0.025D || !robot.isMoving();
            if (distance <= 0.18D || (notProgressing && ++stuckTicks > 8)) {
                path.removeFirst();
                setNextInPath();
            } else {
                stuckTicks = notProgressing ? stuckTicks : 0;
                prevDistance = Math.min(prevDistance, distance);
            }
        }

        if (path != null && path.isEmpty()) {
            robot.setDeltaMovement(Vec3.ZERO);
            if (lastBlockInPath != null) {
                robot.setPos(lastBlockInPath.x + 0.5D, lastBlockInPath.y + 0.5D, lastBlockInPath.z + 0.5D);
            }
            terminate();
        }
    }

    private double distanceTo(double x, double y, double z) {
        double dx = robot.getX() - x;
        double dy = robot.getY() - y;
        double dz = robot.getZ() - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void setNextInPath() {
        if (path != null && !path.isEmpty()) {
            boolean isFirst = prevDistance == Double.MAX_VALUE;
            BlockIndex next = path.getFirst();
            prevDistance = Double.MAX_VALUE;
            stuckTicks = 0;

            // The old robot had noClip, but the pathfinder still refused to route through hard blocks. Re-check the
            // next node because blocks can change while the robot is travelling or after NBT load. The first node is
            // allowed so a robot that is currently embedded in a station/pipe block can escape instead of deadlocking.
            if (isFirst || isSoft(robot.level, next.toBlockPos())) {
                setDestination(robot, next.x + 0.5D, next.y + 0.5D, next.z + 0.5D);
                robot.aimItemAt(next.x, next.y, next.z);
            } else {
                path = null;
                robot.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    private LinkedList<BlockIndex> findPath() {
        BlockPos start = robot.blockPosition();
        BlockPos target = new BlockPos(finalX, finalY, finalZ);
        if (start.equals(target)) {
            LinkedList<BlockIndex> single = new LinkedList<>();
            single.add(new BlockIndex(target));
            return single;
        }

        Level level = robot.level;
        if (!isSoft(level, target)) {
            return null;
        }

        // Most robot paths are just long open-air runs. Try cheap axis-aligned paths first; this avoids the old
        // breadth-first flood from exhausting its node budget after only about one chunk of 3D air.
        LinkedList<BlockIndex> direct = directFallback(start, target, level);
        if (direct != null) {
            return direct;
        }

        return findAStarPath(start, target, level);
    }

    private LinkedList<BlockIndex> findAStarPath(BlockPos start, BlockPos target, Level level) {
        int hardLimit = maxDistance > 0 ? Math.max(1, (int) Math.ceil(maxDistance)) : 256;
        int maxVisited = Math.max(8192, Math.min(262144, hardLimit * hardLimit * 4));
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

        while (!open.isEmpty() && closed.size() < maxVisited) {
            PathNode node = open.poll();
            BlockPos current = node.pos();
            if (!closed.add(current)) {
                continue;
            }
            if (current.equals(target)) {
                return reconstruct(start, target, parent);
            }

            int currentCost = bestCost.getOrDefault(current, Integer.MAX_VALUE);
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (closed.contains(next)) continue;
                if (distanceSqr(next, start) > hardLimit * hardLimit) continue;
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

    private static double distanceSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean isSoft(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private static LinkedList<BlockIndex> reconstruct(BlockPos start, BlockPos target, Map<BlockPos, BlockPos> parent) {
        List<BlockPos> reversed = new ArrayList<>();
        BlockPos cur = target;
        while (cur != null && !cur.equals(start)) {
            reversed.add(cur);
            cur = parent.get(cur);
        }
        Collections.reverse(reversed);
        LinkedList<BlockIndex> out = new LinkedList<>();
        for (BlockPos pos : reversed) out.add(new BlockIndex(pos));
        return out;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
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

    private record PathNode(BlockPos pos, double score, long sequence) {
    }

    @Override
    public void end() {
        robot.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        nbt.putInt("finalX", finalX);
        nbt.putInt("finalY", finalY);
        nbt.putInt("finalZ", finalZ);
        nbt.putDouble("maxDistance", maxDistance);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        finalX = nbt.getInt("finalX");
        finalY = nbt.getInt("finalY");
        finalZ = nbt.getInt("finalZ");
        maxDistance = nbt.getDouble("maxDistance");
        loadedFromNBT = true;
    }
}
