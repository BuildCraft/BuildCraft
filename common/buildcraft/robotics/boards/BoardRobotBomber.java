/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.ArrayStackFilter;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotLoad;
import buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock;
import buildcraft.robotics.ai.path.AIRobotGotoBlock;

public class BoardRobotBomber extends RedstoneBoardRobot {

    private static final IStackFilter TNT_FILTER = new ArrayStackFilter(new ItemStack(Blocks.tnt));

    private int flyingHeight = 20;

    public BoardRobotBomber(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("bomber");
    }

    @Override
    public final void update() {
        boolean containItems = false;

        for (int i = 0; i < robot.getSizeInventory(); ++i) {
            if (robot.getStackInSlot(i) != null) {
                containItems = true;
            }
        }

        if (!containItems) {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, TNT_FILTER, AIRobotLoad.ANY_QUANTITY));
        } else {
            startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, 100, new IBlockFilter() {
                @Override
                public boolean matches(World world, BlockPos pos) {
                    return pos.getY() < world.getActualHeight() - flyingHeight && !world.isAirBlock(pos);
                }
            }, robot.getZoneToWork()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotSearchRandomGroundBlock) {
            if (ai.success()) {
                AIRobotSearchRandomGroundBlock aiFind = (AIRobotSearchRandomGroundBlock) ai;

                startDelegateAI(AIRobotGotoBlock.newGotoBlock(robot, aiFind.blockFound.add(0, flyingHeight, 0)));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (AIRobotGotoBlock.isAIGotoBlock(ai)) {
            if (ai.success()) {
                ITransactor t = Transactor.getTransactorFor(robot, null);
                ItemStack stack = t.remove(TNT_FILTER, true);

                if (stack != null && stack.stackSize > 0) {
                    EntityTNTPrimed tnt = new EntityTNTPrimed(robot.worldObj, robot.posX + 0.25, robot.posY - 1, robot.posZ + 0.25, robot);
                    tnt.fuse = 37;
                    robot.worldObj.spawnEntityInWorld(tnt);
                    robot.worldObj.playSoundAtEntity(tnt, "game.tnt.primed", 1.0F, 1.0F);
                }
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
