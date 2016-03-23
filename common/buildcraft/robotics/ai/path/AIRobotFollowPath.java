package buildcraft.robotics.ai.path;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.robotics.ai.AIRobotGoto;

public class AIRobotFollowPath extends AIRobotGoto {
    private LinkedList<BlockPos> path;
    private BlockPos lastBlockInPath;
    private double prevDistance = Double.MAX_VALUE;
    private long lastShortcutAttempt = -1;

    public AIRobotFollowPath(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotFollowPath(EntityRobotBase robot, List<BlockPos> path) {
        this(robot);
        this.path = new LinkedList<>(path);
        lastBlockInPath = this.path.getLast();
        setNextInPath();
    }

    @Override
    public void update() {
        if (path == null) terminate();

        if (path != null && next != null) {
            double distance = robot.getDistance(next.xCoord, next.yCoord, next.zCoord);

            if (!robot.isMoving() || distance > prevDistance) {
                if (path.size() > 0) {
                    path.removeFirst();
                }

                setNextInPath();
            } else {
                prevDistance = robot.getDistance(next.xCoord, next.yCoord, next.zCoord);

                long now = robot.worldObj.getTotalWorldTime();
                if (lastShortcutAttempt == -1) lastShortcutAttempt = now + BuildCraftCore.random.nextInt(4);
                else if (now - lastShortcutAttempt > 5) {
                    tryToShortcutPath();
                    lastShortcutAttempt = now;
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
    }

    private void tryToShortcutPath() {
        // Try to shortcut the path by looking forward in the path to the next visible node.
    }

    private void setNextInPath() {
        if (path != null && path.size() > 0) {

            boolean isFirst = prevDistance == Double.MAX_VALUE;

            BlockPos next = path.getFirst();
            if (isFirst || BuildCraftAPI.isSoftBlock(robot.worldObj, next)) {
                setDestination(robot, Utils.convertMiddle(next));
                prevDistance = Double.MAX_VALUE;
                robot.aimItemAt(next);
            } else {
                // Path invalid!
                path = null;
            }
        }
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (path != null) {
            NBTTagList pathList = new NBTTagList();

            for (BlockPos i : path) {
                pathList.appendTag(NBTUtils.writeBlockPos(i));
            }

            nbt.setTag("path", pathList);
        }
        nbt.setLong("lastShortcutAttempt", lastShortcutAttempt);
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("path")) {
            NBTTagList pathList = nbt.getTagList("path", Constants.NBT.TAG_COMPOUND);

            path = new LinkedList<>();

            for (int i = 0; i < pathList.tagCount(); ++i) {
                path.add(NBTUtils.readBlockPos(pathList.get(i)));
            }
            lastBlockInPath = path.getLast();
        }
        lastShortcutAttempt = nbt.getLong("lastShortcutAttempt");
    }
}
