package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.crops.CropManager;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotHarvest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** BuildCraft 7.1.x harvester board port. Searches for mature crops and harvests them. */
public class BoardRobotHarvester extends BoardRobotGenericSearchBlock {
    public BoardRobotHarvester(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("harvester").nbt();
    }

    @Override
    public boolean isExpectedBlock(Level level, BlockPos pos) {
        return level.isLoaded(pos) && CropManager.isMature(level, level.getBlockState(pos), pos);
    }

    @Override
    public void update() {
        if (blockFound() != null) {
            startDelegateAI(new AIRobotHarvest(robot, blockFound()));
        } else {
            super.update();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotHarvest) {
            releaseBlockFound(ai.success());
        }
        super.delegateAIEnded(ai);
    }
}
