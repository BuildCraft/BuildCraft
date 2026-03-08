/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IBlockFilter;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.lib.inventory.filter.PassThroughFluidFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

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
        // final IWorldProperty isFluidSource = BuildCraftAPI.getWorldProperty("fluidSource");
        // FluidStack tank = robot.getTankInfo(null)[0].fluid;
        FluidStack tank = robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).getFluidInTank(0);

        // if (tank != null && tank.amount > 0)
        if (!tank.isEmpty()) {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
        } else {
            updateFilter();

            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {

                @Override
                public boolean matches(Level world, BlockPos pos) {
                    // if (isFluidSource.get(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos)))
                    if (world.getBlockState(pos).getFluidState().isSource() && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
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

    private boolean matchesGateFilter(Level world, BlockPos pos) {
        if (fluidFilter == null) {
            return true;
        }

        // Block block;
        BlockState blockState;
        synchronized (world) {
            // block = blockState.getBlock();
            blockState = world.getBlockState(pos);
        }

        // Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        FluidStack fluid = new FluidStack(blockState.getFluidState().getType(), 1);

        return fluidFilter.matches(fluid);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);
        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}
