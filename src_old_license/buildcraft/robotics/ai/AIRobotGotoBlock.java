/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.IterableAlgorithmRunner;
import buildcraft.core.lib.utils.PathFinding;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtilBC;

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
        this(robot);
        finalPos = pos;
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
            pathSearch = new PathFinding(robot.worldObj, new BlockPos((int) Math.floor(robot.posX), (int) Math.floor(robot.posY), (int) Math.floor(
                    robot.posZ)), finalPos, maxDistance);

            pathSearchJob = new IterableAlgorithmRunner(pathSearch, 50);
            pathSearchJob.start();
        } else if (path != null && next != null) {
            double distance = robot.getDistance(next.xCoord, next.yCoord, next.zCoord);

            if (!robot.isMoving() || distance > prevDistance) {
                if (path.size() > 0) {
                    path.removeFirst();
                }

                setNextInPath();
            } else {
                prevDistance = robot.getDistance(next.xCoord, next.yCoord, next.zCoord);
            }
        } else {
            if (pathSearchJob.isDone()) {
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
            robot.motionX = 0;
            robot.motionY = 0;
            robot.motionZ = 0;

            if (lastBlockInPath != null) {
                robot.posX = lastBlockInPath.getX() + 0.5F;
                robot.posY = lastBlockInPath.getY() + 0.5F;
                robot.posZ = lastBlockInPath.getZ() + 0.5F;
            }
            terminate();
        }
    }

    private void setNextInPath() {
        if (path != null && path.size() > 0) {

            boolean isFirst = prevDistance == Double.MAX_VALUE;

            BlockPos next = path.getFirst();
            if (isFirst || PathFinding.isFreeForPath(robot.worldObj, next)) {
                lastBlock = next;
                setDestination(robot, Utils.convertMiddle(next));
                prevDistance = Double.MAX_VALUE;
                robot.aimItemAt(next);
            } else if (skipLastIfNotSoft && path.size() == 1) {
                lastBlockInPath = lastBlock;
            } else {
                // Path invalid!
                path = null;

                if (pathSearchJob != null) {
                    pathSearchJob.terminate();
                    robot.motionX = 0;
                    robot.motionY = 0;
                    robot.motionZ = 0;
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
            robot.motionX = 0;
            robot.motionY = 0;
            robot.motionZ = 0;
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        nbt.setTag("finalPos", NBTUtilBC.writeBlockPos(finalPos));
        nbt.setDouble("maxDistance", maxDistance);
        nbt.setBoolean("skipLastIfNotSoft", skipLastIfNotSoft);
        
        if (path != null) {
            NBTTagList pathList = new NBTTagList();

            for (BlockPos i : path) {
                pathList.appendTag(NBTUtilBC.writeBlockPos(i));
            }

            nbt.setTag("path", pathList);
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        finalPos = NBTUtilBC.readBlockPos(nbt.getTag("finalPos"));
        maxDistance = nbt.getDouble("maxDistance");
        skipLastIfNotSoft = nbt.getBoolean("skipLastIfNotSoft");
        
        if (nbt.hasKey("path")) {
            NBTTagList pathList = nbt.getTagList("path", Constants.NBT.TAG_COMPOUND);

            path = new LinkedList<>();

            for (int i = 0; i < pathList.tagCount(); ++i) {
                path.add(NBTUtilBC.readBlockPos(pathList.get(i)));
            }
        }

        loadedFromNBT = true;
    }
}
