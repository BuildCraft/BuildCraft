package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotAttack;
import ct.buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import ct.buildcraft.robotics.ai.AIRobotSearchEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/** BuildCraft 7.1.x butcher board port: fetches a sword, then hunts passive animals in the work zone. */
public class BoardRobotButcher extends RedstoneBoardRobot {
    private static final float SEARCH_RANGE = 250.0F;

    public BoardRobotButcher(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("butcher").nbt();
    }

    @Override
    public final void update() {
        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        if (held.isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new SwordFilter()));
        } else if (held.isDamageableItem() && held.getDamageValue() >= held.getMaxDamage()) {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot));
        } else {
            startDelegateAI(new AIRobotSearchEntity(robot, entity -> entity instanceof Animal, SEARCH_RANGE, robot.getZoneToWork()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotSearchEntity search) {
            if (search.success()) {
                startDelegateAI(new AIRobotAttack(robot, search.target));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    private static final class SwordFilter implements IStackFilter {
        @Override
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty()
                    && stack.getItem() instanceof SwordItem
                    && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage());
        }
    }
}
