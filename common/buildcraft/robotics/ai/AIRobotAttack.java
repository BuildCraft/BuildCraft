/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.VecUtil;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.entity.Entity;

public class AIRobotAttack extends AIRobot {

    private Entity target;

    private int delay = 10;

    public AIRobotAttack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotAttack(EntityRobotBase iRobot, Entity iTarget) {
        this(iRobot);

        target = iTarget;
    }

    @Override
    public void preempt(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            // target may become null in the event of a load. In that case, just
            // go to the expected location.
//            if (target != null && robot.getDistanceToEntity(target) <= 2.0)
            if (target != null && robot.distanceTo(target) <= 2.0) {
                abortDelegateAI();
                robot.setItemActive(true);
            }
        }
    }

    @Override
    public void update() {
//        if (target == null || target.isDead)
        if (target == null || !target.isAlive()) {
            terminate();
            return;
        }

        if (robot.distanceTo(target) > 2.0) {
            // startDelegateAI(new AIRobotGotoBlock(robot, VecUtil.getPos(target)));
            // Calen Fix 1.18.2: if the target is in a non-soft block, the robot may be stuck beside the position of the target
            startDelegateAI(new AIRobotGotoBlock(robot, VecUtil.getPos(target), true));
            robot.setItemActive(false);

            return;
        }

        delay++;

        if (delay > 20) {
            delay = 0;
            ((EntityRobot) robot).attackTargetEntityWithCurrentItem(target);
            robot.aimItemAt(VecUtil.getPos(target));
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (!ai.success()) {
                robot.unreachableEntityDetected(target);
            }
            terminate();
        }
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        // return BuilderAPI.BREAK_ENERGY * 2 / 20;
        return 16 * MjAPI.MJ * 2 / 20;
    }
}
