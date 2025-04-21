/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.IterableAlgorithmRunner;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.pathfinding.PathFinding;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedList;

public class AIRobotGotoBlock extends AIRobotGoto {

    private PathFinding pathSearch;
    private IterableAlgorithmRunner pathSearchJob;
    private LinkedList<BlockPos> path;
    private double prevDistance = Double.MAX_VALUE;
    private BlockPos finalPos;
    private double maxDistance = 0;
    private BlockPos lastBlockInPath;
    private boolean loadedFromNBT;
    private boolean skipLastIfNotSoft;
    private BlockPos lastBlock;

    public AIRobotGotoBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos) {
        this(robot);
        finalPos = pos;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos, double iMaxDistance) {
        this(robot, pos);
        maxDistance = iMaxDistance;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> iPath) {
        this(robot);
        path = iPath;
        finalPos = path.getLast();
        setNextInPath();
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos, boolean iSkipLastIfNotSoft) {
        this(robot, pos);
        skipLastIfNotSoft = iSkipLastIfNotSoft;
    }

    @Override
    public void start() {
        robot.undock();
    }

    @Override
    public void update() {
        if (loadedFromNBT) {
            // Prevent a race condition with terminate() being called in
            // setNextInPath.
            setNextInPath();
            loadedFromNBT = false;
        }

        if (path == null && pathSearch == null) {
            pathSearch = new PathFinding(robot.level, new BlockPos((int) Math.floor(robot.getX()), (int) Math.floor(robot.getY()), (int) Math.floor(robot.getZ())), finalPos, maxDistance);

            pathSearchJob = new IterableAlgorithmRunner(pathSearch, 50);
            pathSearchJob.start();
        } else if (path != null && next != null) {
            // double distance = robot.getDistance(next.x, next.y, next.z);
            double distance = Math.sqrt(robot.distanceToSqr(next.x, next.y, next.z));

            if (!robot.isMoving() || distance > prevDistance) {
                if (path.size() > 0) {
                    path.removeFirst();
                }

                setNextInPath();
            } else {
                // prevDistance = robot.getDistance(next.x, next.y, next.z);
                prevDistance = Math.sqrt(robot.distanceToSqr(next.x, next.y, next.z));
            }
        } else {
            // if (pathSearchJob.isDone())
            if (pathSearchJob != null && pathSearchJob.isDone()) {
                path = pathSearch.getResult();

                if (path.size() == 0) {
                    setSuccess(false);
                    terminate();
                    return;
                }

                lastBlockInPath = path.getLast();

                setNextInPath();
            }
        }

        if (path != null && path.size() == 0) {
//            robot.motionX = 0;
//            robot.motionY = 0;
//            robot.motionZ = 0;
            robot.setDeltaMovement(0, 0, 0);

            if (lastBlockInPath != null) {
//                robot.posX = lastBlockInPath.getX() + 0.5F;
//                robot.posY = lastBlockInPath.getY() + 0.5F;
//                robot.posZ = lastBlockInPath.getZ() + 0.5F;
                robot.setPos(lastBlockInPath.getX() + 0.5F, lastBlockInPath.getY() + 0.5F, lastBlockInPath.getZ() + 0.5F);
            }
            terminate();
        }
    }

    private void setNextInPath() {
        if (path != null && path.size() > 0) {

            boolean isFirst = prevDistance == Double.MAX_VALUE;

            BlockPos next = path.getFirst();
            // if (isFirst || BuildCraftAPI.isSoftBlock(robot.level, next))
            if (isFirst || BlockUtil.isSoftBlock(robot.level, next)) {
                lastBlock = next;
                setDestination(robot, VecUtil.convertCenter(next));
                prevDistance = Double.MAX_VALUE;
                robot.aimItemAt(next);
            } else if (skipLastIfNotSoft && path.size() == 1) {
                lastBlockInPath = lastBlock;
            } else {
                // Path invalid!
                path = null;

                if (pathSearchJob != null) {
                    pathSearchJob.terminate();
//                    robot.motionX = 0;
//                    robot.motionY = 0;
//                    robot.motionZ = 0;
                    robot.setDeltaMovement(0, 0, 0);
                }

                // If the Path is invalid it should be recalculated or this AI should fail
                pathSearch = null;
                pathSearchJob = null;
            }
        }
    }

    @Override
    public void end() {
        if (pathSearchJob != null) {
            pathSearchJob.terminate();
//            robot.motionX = 0;
//            robot.motionY = 0;
//            robot.motionZ = 0;
            robot.setDeltaMovement(0, 0, 0);
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        nbt.put("finalPos", NBTUtilBC.writeBlockPos(finalPos));
        nbt.putDouble("maxDistance", maxDistance);
        nbt.putBoolean("skipLastIfNotSoft", skipLastIfNotSoft);

        if (path != null) {
            ListTag pathList = new ListTag();

            for (BlockPos i : path) {
                pathList.add(NBTUtilBC.writeBlockPos(i));
            }

            nbt.put("path", pathList);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        finalPos = NBTUtilBC.readBlockPos(nbt.get("finalPos"));
        maxDistance = nbt.getDouble("maxDistance");
        skipLastIfNotSoft = nbt.getBoolean("skipLastIfNotSoft");

        if (nbt.contains("path")) {
            ListTag pathList = nbt.getList("path", Tag.TAG_COMPOUND);

            path = new LinkedList<BlockPos>();

            for (int i = 0; i < pathList.size(); ++i) {
                path.add(NBTUtilBC.readBlockPos(pathList.get(i)));
            }
        }

        loadedFromNBT = true;
    }
}
