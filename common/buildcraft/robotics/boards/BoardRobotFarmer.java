/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.ai.AIRobotUseToolOnBlock;

public class BoardRobotFarmer extends RedstoneBoardRobot {

    private BlockPos blockFound;

    public BoardRobotFarmer(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("farmer");
    }

    @Override
    public void update() {
        final IWorldProperty isDirt = BuildCraftAPI.getWorldProperty("dirt");
        if (robot.getHeldItem() == null) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
                @Override
                public boolean matches(ItemStack stack) {
                    return stack != null && stack.getItem() instanceof ItemHoe;
                }
            }));
        } else {
            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {
                @Override
                public boolean matches(World world, BlockPos pos) {
                    return isDirt.get(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos)) && isAirAbove(world, pos);
                }
            }));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock) {
            if (ai.success()) {
                blockFound = ((AIRobotSearchAndGotoBlock) ai).getBlockFound();
                startDelegateAI(new AIRobotUseToolOnBlock(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotUseToolOnBlock) {
            releaseBlockFound();
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    @Override
    public void end() {
        releaseBlockFound();
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.setTag("blockFound", NBTUtils.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("blockFound")) {
            blockFound = NBTUtils.readBlockPos(nbt.getTag("blockFound"));
        }
    }

    private boolean isAirAbove(World world, BlockPos pos) {
        synchronized (world) {
            return world.isAirBlock(pos.up());
        }
    }
}
