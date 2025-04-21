/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public class AIRobotStraightMoveTo extends AIRobotGoto {

    private double prevDistance = Double.MAX_VALUE;

    private Vector3d pos;

    public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStraightMoveTo(EntityRobotBase iRobot, Vector3d pos) {
        this(iRobot);
        this.pos = pos;
        robot.aimItemAt(VecUtil.convertFloor(pos));
    }

    @Override
    public void start() {
        robot.undock();
        setDestination(robot, pos);
    }

    @Override
    public void update() {
        double distance = VecUtil.getVec(robot).distanceTo(next);

        if (distance < prevDistance) {
            prevDistance = distance;
        } else {
//            robot.motionX = 0;
//            robot.motionY = 0;
//            robot.motionZ = 0;
            robot.setDeltaMovement(Vector3d.ZERO);

//            robot.posX = pos.x;
//            robot.posY = pos.y;
//            robot.posZ = pos.z;
            robot.setPos(pos.x, pos.y, pos.z);

            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundNBT nbt) {
        super.writeSelfToNBT(nbt);

        nbt.putFloat("x", (float) pos.x);
        nbt.putFloat("y", (float) pos.y);
        nbt.putFloat("z", (float) pos.z);
    }

    @Override
    public void loadSelfFromNBT(CompoundNBT nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("x")) {
            float x = nbt.getFloat("x");
            float y = nbt.getFloat("y");
            float z = nbt.getFloat("z");
            pos = new Vector3d(x, y, z);
        }
    }
}
