/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.world.phys.Vec3;

public abstract class AIRobotGoto extends AIRobot {

    protected Vec3 next, dir;

    public AIRobotGoto(EntityRobotBase iRobot) {
        super(iRobot);
    }

    protected void setDestination(EntityRobotBase robot, Vec3 dest) {
        next = dest;
        // dir = next.subtract(robot.posX, robot.posY, robot.posZ);
        dir = next.subtract(robot.position());

        // double magnitude = dir.lengthVector();
        double magnitude = dir.length();

        if (magnitude != 0) {
            // dir = VecUtil.multiply(dir, 1 / magnitude);
            dir = VecUtil.scale(dir, 1 / magnitude);
        } else {
            dir = new Vec3(0, 0, 0);
        }

//        robot.motionX = dir.x / 10f;
//        robot.motionY = dir.y / 10f;
//        robot.motionZ = dir.z / 10f;
        robot.setDeltaMovement(dir.x / 10f, dir.y / 10f, dir.z / 10f);
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 3 * MjAPI.MJ / 10;
    }
}
