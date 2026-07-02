package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.lib.inventory.filter.ArrayFluidFilter;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import ct.buildcraft.robotics.statements.ActionStationProvideFluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class AIRobotLoadFluids extends AIRobot {
    private IFluidFilter filter;
    private int waitedCycles;

    public AIRobotLoadFluids(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
        this(robot);
        this.filter = filter;
        setSuccess(false);
    }

    @Override
    public void update() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }

        waitedCycles++;
        if (waitedCycles > 40) {
            int loaded = load(robot, robot.getDockingStation(), filter, true);
            if (loaded <= 0) {
                terminate();
            } else {
                setSuccess(true);
                waitedCycles = 0;
            }
        }
    }

    public static int load(EntityRobotBase robot, DockingStation station, IFluidFilter filter, boolean doLoad) {
        if (robot == null || station == null || filter == null) {
            return 0;
        }

        IFluidHandler handler = station.getFluidInput();
        if (handler == null) {
            return 0;
        }

        FluidStack drainable = handler.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (drainable.isEmpty() || !filter.matches(drainable)) {
            return 0;
        }
        if (!ActionRobotFilter.canInteractWithFluid(station, new ArrayFluidFilter(drainable), ActionStationProvideFluids.class)) {
            return 0;
        }

        int fillable = robot.fill(drainable, FluidAction.SIMULATE);
        if (fillable <= 0) {
            return 0;
        }

        FluidStack toDrain = drainable.copy();
        toDrain.setAmount(Math.min(toDrain.getAmount(), fillable));
        FluidStack drained = handler.drain(toDrain, doLoad ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        if (drained.isEmpty()) {
            return 0;
        }

        int filled = robot.fill(drained, doLoad ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        if (doLoad && filled < drained.getAmount()) {
            // This should not happen because the operation is simulated first, but report the accepted amount rather
            // than looping forever if a mutable external tank changed between simulate and execute.
            return filled;
        }
        return filled;
    }

    @Override
    public int getEnergyCost() {
        return 8;
    }
}
