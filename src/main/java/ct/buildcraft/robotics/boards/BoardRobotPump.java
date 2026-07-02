package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.lib.inventory.filter.PassThroughFluidFilter;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import ct.buildcraft.robotics.ai.AIRobotPumpBlock;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BoardRobotPump extends RedstoneBoardRobot {
    private BlockIndex blockFound;
    private IFluidFilter fluidFilter;

    public BoardRobotPump(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("pump").nbt();
    }

    @Override
    public void update() {
        if (isTankFull()) {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
            return;
        }

        updateFilter();
        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, this::matchesPumpTarget));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (search.success()) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotPumpBlock(robot, blockFound));
            } else if (hasFluidInTank()) {
                startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPumpBlock) {
            releaseBlockFound();

            if (isTankFull()) {
                startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
            } else if (!ai.success() && hasFluidInTank()) {
                startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
            }
        } else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public void end() {
        releaseBlockFound();
    }

    private boolean isTankFull() {
        FluidStack tank = robot.getFluidInTank(0);
        return !tank.isEmpty() && tank.getAmount() >= robot.getTankCapacity(0);
    }

    private boolean hasFluidInTank() {
        FluidStack tank = robot.getFluidInTank(0);
        return !tank.isEmpty() && tank.getAmount() > 0;
    }

    private boolean matchesPumpTarget(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        if (robot.getRegistry() != null && robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
            return false;
        }

        FluidStack fluid = BlockUtil.drainBlock(level, pos, false);
        if (fluid.isEmpty()) {
            return false;
        }
        if (robot.fill(fluid, FluidAction.SIMULATE) <= 0) {
            return false;
        }
        return matchesGateFilter(fluid);
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            if (robot.getRegistry() != null) {
                robot.getRegistry().release(new ResourceIdBlock(blockFound));
            }
            blockFound = null;
        }
    }

    private void updateFilter() {
        fluidFilter = ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
        if (fluidFilter instanceof PassThroughFluidFilter) {
            fluidFilter = null;
        }
    }

    private boolean matchesGateFilter(FluidStack fluid) {
        return fluidFilter == null || fluidFilter.matches(fluid);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("blockFound", tag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
    }
}
