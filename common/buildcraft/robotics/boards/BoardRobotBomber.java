/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IBlockFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.ai.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class BoardRobotBomber extends RedstoneBoardRobot {

    private static final IStackFilter TNT_FILTER = new ArrayStackFilter(new ItemStack(Blocks.TNT));

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

        // for (int i = 0; i < robot.getSizeInventory(); ++i)
        IItemHandler itemHandler = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            // if (robot.getStackInSlot(i) != null)
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                containItems = true;
            }
        }

        if (!containItems) {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, TNT_FILTER, AIRobotLoad.ANY_QUANTITY));
        } else {
            startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, 100, new IBlockFilter() {
                @Override
                public boolean matches(World world, BlockPos pos) {
                    // return pos.getY() < world.getActualHeight() - flyingHeight && !world.isEmptyBlock(pos);
                    return pos.getY() < world.getMaxBuildHeight() - flyingHeight && !world.isEmptyBlock(pos);
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

                startDelegateAI(new AIRobotGotoBlock(robot, aiFind.blockFound.offset(0, flyingHeight, 0)));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                // ITransactor t = Transactor.getTransactorFor(robot);
                IItemHandler t = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);
                // ItemStack stack = t.remove(TNT_FILTER, null, true);
                ItemStack stack = TNT_FILTER.matches(t.getStackInSlot(0)) ? t.extractItem(0, 1, false) : StackUtil.EMPTY;

                // if (stack != null && stack.getCount() > 0)
                if (!stack.isEmpty() && stack.getCount() > 0) {
                    TNTEntity tnt = new TNTEntity(robot.level, robot.getX() + 0.25, robot.getY() - 1, robot.getZ() + 0.25, robot);
                    // tnt.fuse = 37;
                    tnt.setFuse(37);
                    robot.level.addFreshEntity(tnt);
                    // robot.level.playSoundAtEntity(tnt, "game.tnt.primed", 1.0F, 1.0F);
                    robot.level.playSound(null, tnt, SoundEvents.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
