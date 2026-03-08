/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IEntityFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class BoardRobotKnight extends RedstoneBoardRobot {

    public BoardRobotKnight(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("knight");
    }

    @Override
    public final void update() {
        // if (robot.getHeldItem() == null)
        if (robot.getMainHandItem().isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
                @Override
                public boolean matches(ItemStack stack) {
                    return stack.getItem() instanceof SwordItem;
                }
            }));
        }
        // else if (robot.getHeldItem() != null && robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage())
        else if (!robot.getMainHandItem().isEmpty() && robot.getMainHandItem().getDamageValue() >= robot.getMainHandItem().getMaxDamage()) {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot));
        } else {
            startDelegateAI(new AIRobotSearchEntity(robot, new IEntityFilter() {
                @Override
                public boolean matches(Entity entity) {
                    return (entity instanceof Monster) || (entity instanceof Wolf && ((Wolf) entity).isAngry());
                }
            }, 250, robot.getZoneToWork()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotSearchEntity) {
            if (ai.success()) {
                startDelegateAI(new AIRobotAttack(robot, ((AIRobotSearchEntity) ai).target));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
