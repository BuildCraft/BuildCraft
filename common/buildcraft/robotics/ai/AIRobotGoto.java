/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.util.Vec3;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.Utils;

public abstract class AIRobotGoto extends AIRobot {

    protected Vec3 next, dir;

    public AIRobotGoto(EntityRobotBase iRobot) {
        super(iRobot);
    }

    protected void setDestination(EntityRobotBase robot, Vec3 dest) {
        next = dest;
        dir = next.subtract(robot.posX, robot.posY, robot.posZ);

        double magnitude = dir.lengthVector();

        if (magnitude != 0) {
            dir = Utils.multiply(dir, 1 / magnitude);
        } else {
            dir = new Vec3(0, 0, 0);
        }

        robot.motionX = dir.xCoord / 10f;
        robot.motionY = dir.yCoord / 10f;
        robot.motionZ = dir.zCoord / 10f;
    }

    @Override
    public int getEnergyCost() {
        return 3;
    }
}
