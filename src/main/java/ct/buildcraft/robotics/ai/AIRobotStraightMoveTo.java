package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class AIRobotStraightMoveTo extends AIRobotGoto {
    private double prevDistance = Double.MAX_VALUE;
    private double x, y, z;

    public AIRobotStraightMoveTo(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotStraightMoveTo(EntityRobotBase robot, double x, double y, double z) {
        this(robot);
        this.x = x;
        this.y = y;
        this.z = z;
        robot.aimItemAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    @Override
    public void start() {
        robot.undock();
        setDestination(robot, x, y, z);
    }

    @Override
    public void update() {
        double d = Math.sqrt(robot.distanceToSqr(x, y, z));
        if (d < 0.05D || d >= prevDistance) {
            robot.setDeltaMovement(Vec3.ZERO);
            robot.setPos(x, y, z);
            terminate();
        } else {
            prevDistance = d;
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        nbt.putDouble("z", z);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
    }
}
