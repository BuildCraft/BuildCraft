/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.core.lib.inventory.filters.PassThroughFluidFilter;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotPump extends RedstoneBoardRobot {

    private BlockPos blockFound;
    private IFluidFilter fluidFilter = null;

    public BoardRobotPump(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("pump");
    }

    @Override
    public void update() {
        final IWorldProperty isFluidSource = BuildCraftAPI.getWorldProperty("fluidSource");
        FluidStack tank = robot.getTankInfo(null)[0].fluid;

        if (tank != null && tank.amount > 0) {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
        } else {
            updateFilter();

            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {

                @Override
                public boolean matches(World world, BlockPos pos) {
                    if (isFluidSource.get(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
                        return matchesGateFilter(world, pos);
                    } else {
                        return false;
                    }
                }
            }));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock) {
            if (ai.success()) {
                blockFound = ((AIRobotSearchAndGotoBlock) ai).getBlockFound();
                startDelegateAI(new AIRobotPumpBlock(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPumpBlock) {
            releaseBlockFound();
        } else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {

            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    public void updateFilter() {
        fluidFilter = ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
        if (fluidFilter instanceof PassThroughFluidFilter) {
            fluidFilter = null;
        }
    }

    private boolean matchesGateFilter(World world, BlockPos pos) {
        if (fluidFilter == null) {
            return true;
        }

        Block block;
        synchronized (world) {
            block = world.getBlockState(pos).getBlock();
        }

        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

        return fluidFilter.matches(fluid);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
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
}
