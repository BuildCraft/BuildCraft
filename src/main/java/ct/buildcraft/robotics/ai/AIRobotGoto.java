package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.world.phys.Vec3;

public abstract class AIRobotGoto extends AIRobot {
    protected double nextX, nextY, nextZ;
    protected double dirX, dirY, dirZ;

    public AIRobotGoto(EntityRobotBase robot) {
        super(robot);
    }

    protected void setDestination(EntityRobotBase robot, double x, double y, double z) {
        nextX = x;
        nextY = y;
        nextZ = z;
        dirX = nextX - robot.getX();
        dirY = nextY - robot.getY();
        dirZ = nextZ - robot.getZ();
        double mag = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (mag != 0) {
            dirX /= mag;
            dirY /= mag;
            dirZ /= mag;
        } else {
            dirX = dirY = dirZ = 0;
        }
        robot.setDeltaMovement(new Vec3(dirX / 10.0D, dirY / 10.0D, dirZ / 10.0D));
    }

    @Override
    public int getEnergyCost() {
        return 3;
    }
}
