/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.Utils;

public class AIRobotStraightMoveTo extends AIRobotGoto {

    private double prevDistance = Double.MAX_VALUE;

    private Vec3d pos;

    public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStraightMoveTo(EntityRobotBase iRobot, Vec3d pos) {
        this(iRobot);
        this.pos = pos;
        robot.aimItemAt(Utils.convertFloor(pos));
    }

    @Override
    public void start() {
        robot.undock();
        setDestination(robot, pos);
    }

    @Override
    public void update() {
        double distance = Utils.getVec(robot).distanceTo(next);

        if (distance < prevDistance) {
            prevDistance = distance;
        } else {
            robot.motionX = 0;
            robot.motionY = 0;
            robot.motionZ = 0;

            robot.posX = pos.xCoord;
            robot.posY = pos.yCoord;
            robot.posZ = pos.zCoord;

            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        nbt.setFloat("x", (float) pos.xCoord);
        nbt.setFloat("y", (float) pos.yCoord);
        nbt.setFloat("z", (float) pos.zCoord);
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("x")) {
            float x = nbt.getFloat("x");
            float y = nbt.getFloat("y");
            float z = nbt.getFloat("z");
            pos = new Vec3d(x, y, z);
        }
    }
}
