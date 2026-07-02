package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.StatementSlot;
import ct.buildcraft.lib.inventory.filter.SimpleFluidFilter;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import ct.buildcraft.robotics.statements.ActionStationAcceptFluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class AIRobotUnloadFluids extends AIRobot {
    private int waitedCycles;
    private boolean requireAcceptAction;

    public AIRobotUnloadFluids(EntityRobotBase robot) {
        super(robot);
        setSuccess(false);
    }

    public AIRobotUnloadFluids(EntityRobotBase robot, boolean requireAcceptAction) {
        this(robot);
        this.requireAcceptAction = requireAcceptAction;
    }

    @Override
    public void update() {
        waitedCycles++;
        if (waitedCycles <= 40) {
            return;
        }

        int moved = unload(robot, robot.getDockingStation(), true, requireAcceptAction);
        if (moved > 0) {
            // Keep trying every tick after the initial 40 tick docking delay.
            // The original BuildCraft 7.1.x robot does not wait 40 ticks per bucket;
            // it waits once, then pushes fluid as fast as the target pipe can accept it.
            setSuccess(true);
        } else {
            setSuccess(robot.getFluidInTank(0).isEmpty());
            terminate();
        }
    }

    public static int unload(EntityRobotBase robot, DockingStation station, boolean doUnload) {
        return unload(robot, station, doUnload, false);
    }

    public static int unload(EntityRobotBase robot, DockingStation station, boolean doUnload, boolean requireAcceptAction) {
        if (robot == null || station == null) {
            return 0;
        }

        FluidStack fluidInRobot = robot.getFluidInTank(0);
        if (fluidInRobot.isEmpty() || !canUnloadFluid(station, new SimpleFluidFilter(fluidInRobot), requireAcceptAction)) {
            return 0;
        }

        IFluidHandler fluidHandler = station.getFluidOutput();
        if (fluidHandler == null) {
            return 0;
        }

        FluidStack drainable = robot.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (drainable.isEmpty()) {
            return 0;
        }

        int filled = fluidHandler.fill(drainable.copy(), doUnload ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        if (filled > 0 && doUnload) {
            FluidStack reallyDrained = drainable.copy();
            reallyDrained.setAmount(filled);
            robot.drain(reallyDrained, FluidAction.EXECUTE);
        }
        return filled;
    }

    private static boolean canUnloadFluid(DockingStation station, IFluidFilter filter, boolean requireAcceptAction) {
        boolean hasExplicitAcceptAction = false;
        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionStationAcceptFluids) {
                hasExplicitAcceptAction = true;
                break;
            }
        }

        if (hasExplicitAcceptAction) {
            return ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationAcceptFluids.class);
        }

        if (requireAcceptAction) {
            return false;
        }

        // A plain robot station on a fluid pipe should be usable without forcing the player to add a gate action.
        return station.getFluidOutput() != null;
    }

    @Override
    public int getEnergyCost() {
        return 10;
    }
}
