package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AIRobotShutdown extends AIRobot {
    private int skip;
    private final double motionX;
    private final double motionZ;

    public AIRobotShutdown(EntityRobotBase robot) {
        super(robot);
        Vec3 motion = robot.getDeltaMovement();
        motionX = motion.x;
        motionZ = motion.z;
    }

    @Override
    public void start() {
        robot.undock();
        robot.setDeltaMovement(motionX, -0.075D, motionZ);
    }

    private boolean isBlocked(double yOffset) {
        Vec3 motion = robot.getDeltaMovement();
        AABB nextBox = robot.getBoundingBox().move(motion.x, yOffset, motion.z);
        return !robot.level.noCollision(robot, nextBox);
    }

    @Override
    public void update() {
        if (skip > 0) {
            skip--;
            return;
        }

        if (!isBlocked(-0.075D)) {
            robot.setDeltaMovement(motionX, -0.075D, motionZ);
            return;
        }

        int attempts = 0;
        while (isBlocked(0.0D) && attempts++ < 32) {
            robot.setPos(robot.getX(), robot.getY() + 0.075D, robot.getZ());
        }

        if (Math.abs(motionX) > 1.0E-7D || Math.abs(motionZ) > 1.0E-7D) {
            robot.setDeltaMovement(0.0D, 0.0D, 0.0D);
        } else {
            robot.setDeltaMovement(Vec3.ZERO);
            skip = 20;
        }
    }

    @Override
    public int getEnergyCost() {
        return 0;
    }
}
