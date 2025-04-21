/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.vector.Vector3d;

public class AIRobotShutdown extends AIRobot {
    private int skip;
    private double motionX;
    private double motionZ;

    public AIRobotShutdown(EntityRobotBase iRobot) {
        super(iRobot);
        skip = 0;
        // motionX = robot.motionX;
        // motionZ = robot.motionZ;
        motionX = robot.getDeltaMovement().x();
        motionZ = robot.getDeltaMovement().z();
    }

    @Override
    public void start() {
        robot.undock();
//        robot.motionX = motionX;
//        robot.motionY = -0.075f;
//        robot.motionZ = motionZ;
        robot.setDeltaMovement(motionX, -0.075f, motionZ);
    }

    private boolean isBlocked(float yOffset) {
        // return robot.worldObj.getCollidingBoundingBoxes(robot, robot.getEntityBoundingBox().addCoord(robot.motionX, yOffset, robot.motionZ)).size() > 0;
        return !robot.level.noCollision(robot, robot.getBoundingBox().move(robot.getDeltaMovement().x(), yOffset, robot.getDeltaMovement().z()));
    }

    @Override
    public void update() {
        if (skip == 0) {
            // List<?> boxes = robot.level.getCollidingBoundingBoxes(robot, robot.getEntityBoundingBox().addCoord(robot.motionX, -0.075f, robot.motionZ));
            if (!isBlocked(-0.075f)) {
                // robot.motionY = -0.075f;
                robot.setDeltaMovement(robot.getDeltaMovement().x(), -0.075f, robot.getDeltaMovement().z());
            } else {
                while (isBlocked(0)) {
                    // robot.posY += 0.075f;
                    robot.move(MoverType.SELF, new Vector3d(0, 0.075f, 0));
                }
                // robot.motionY = 0f;
                robot.setDeltaMovement(robot.getDeltaMovement().x(), 0f, robot.getDeltaMovement().z());
                // if (robot.motionX != 0 || robot.motionZ != 0)
                if (robot.getDeltaMovement().x() != 0 || robot.getDeltaMovement().z() != 0) {
                    // robot.motionX = 0f;
                    // robot.motionZ = 0f;
                    robot.setDeltaMovement(0f, robot.getDeltaMovement().y(), 0f);
                    skip = 0;
                } else {
                    skip = 20;
                }
            }
        } else {
            skip--;
        }

    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 0;
    }
}
