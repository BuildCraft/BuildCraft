package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotGotoBlock;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import ct.buildcraft.robotics.ai.AIRobotLoad;
import ct.buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Classic BuildCraft Bomber robot: loads TNT, flies over random ground in its work zone, and drops primed TNT. */
public class BoardRobotBomber extends RedstoneBoardRobot {
    private static final IStackFilter TNT_FILTER = new ArrayStackOrListFilter(new ItemStack(Items.TNT));
    private static final int SEARCH_RANGE = 100;
    private static final int FLYING_HEIGHT = 20;
    private static final int TNT_FUSE = 37;

    public BoardRobotBomber(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("bomber").nbt();
    }

    @Override
    public void update() {
        if (!hasTnt()) {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, TNT_FILTER, 1));
            return;
        }

        startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, SEARCH_RANGE, this::isValidBombTarget, robot.getZoneToWork()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotSearchRandomGroundBlock search) {
            if (search.success()) {
                startDelegateAI(new AIRobotGotoBlock(robot, search.blockFound.x, search.blockFound.y + FLYING_HEIGHT,
                        search.blockFound.z));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                dropTnt();
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoSleep) {
            terminate();
        }
    }

    private boolean isValidBombTarget(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        if (pos.getY() >= level.getMaxBuildHeight() - FLYING_HEIGHT) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return !state.isAir();
    }

    private boolean hasTnt() {
        for (int slot = 0; slot < robot.getContainerSize(); slot++) {
            ItemStack stack = robot.getItem(slot);
            if (!stack.isEmpty() && TNT_FILTER.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean removeOneTnt() {
        for (int slot = 0; slot < robot.getContainerSize(); slot++) {
            ItemStack stack = robot.getItem(slot);
            if (!stack.isEmpty() && TNT_FILTER.matches(stack)) {
                robot.removeItem(slot, 1);
                robot.setChanged();
                return true;
            }
        }
        return false;
    }

    private void dropTnt() {
        if (robot.level.isClientSide || !removeOneTnt()) {
            return;
        }

        PrimedTnt tnt = new PrimedTnt(robot.level, robot.getX() + 0.25D, robot.getY() - 1.0D,
                robot.getZ() + 0.25D, robot);
        tnt.setFuse(TNT_FUSE);
        robot.level.addFreshEntity(tnt);
        robot.level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
