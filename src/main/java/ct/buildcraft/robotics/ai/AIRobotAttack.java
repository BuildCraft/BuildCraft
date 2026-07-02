package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/** BuildCraft 7.1.x AIRobotAttack port. Moves into melee range and attacks with the equipped robot tool. */
public class AIRobotAttack extends AIRobot {
    private Entity target;
    private int delay = 10;

    public AIRobotAttack(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotAttack(EntityRobotBase robot, Entity target) {
        this(robot);
        this.target = target;
    }

    @Override
    public void preempt(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock && target != null && target.isAlive() && robot.distanceToSqr(target) <= 4.0D) {
            abortDelegateAI();
            robot.setItemActive(true);
        }
    }

    @Override
    public void update() {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            terminate();
            return;
        }

        if (robot.distanceToSqr(target) > 4.0D) {
            BlockPos targetPos = target.blockPosition();
            startDelegateAI(new AIRobotGotoBlock(robot, targetPos.getX(), targetPos.getY(), targetPos.getZ()));
            robot.setItemActive(false);
            return;
        }

        robot.setItemActive(true);
        delay++;

        if (delay > 20) {
            delay = 0;
            if (robot instanceof EntityRobot entityRobot) {
                entityRobot.attackTargetEntityWithCurrentItem(target);
            }
            robot.aimItemAt(target.blockPosition());
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (!ai.success() && target != null) {
                robot.unreachableEntityDetected(target);
            }
            terminate();
        }
    }

    @Override
    public int getEnergyCost() {
        return 16;
    }
}
