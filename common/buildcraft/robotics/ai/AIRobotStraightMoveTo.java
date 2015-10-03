/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.util.Vec3;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.Utils;

public class AIRobotStraightMoveTo extends AIRobotGoto {

    private double prevDistance = Double.MAX_VALUE;

    private Vec3 pos;

    public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStraightMoveTo(EntityRobotBase iRobot, Vec3 pos) {
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
}
