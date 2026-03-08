/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.CapUtil;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideFluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class AIRobotLoadFluids extends AIRobot {

    private int waitedCycles = 0;
    private IFluidFilter filter;

    public AIRobotLoadFluids(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
        this(iRobot);

        filter = iFilter;
        setSuccess(false);
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
            return;
        }

        waitedCycles++;

        if (waitedCycles > 40) {
            if (load(robot, robot.getDockingStation(), filter, IFluidHandler.FluidAction.EXECUTE) == 0) {
                terminate();
            } else {
                setSuccess(true);
                waitedCycles = 0;
            }
        }
    }

    // public static int load(EntityRobotBase robot, DockingStation station, IFluidFilter filter, boolean doLoad)
    public static int load(EntityRobotBase robot, DockingStation station, IFluidFilter filter, IFluidHandler.FluidAction doLoad) {
        if (station == null) {
            return 0;
        }

        if (!ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationProvideFluids.class)) {
            return 0;
        }

        IFluidHandler handler = station.getFluidInput();
        if (handler == null) {
            return 0;
        }

//        Direction side = station.getFluidInputSide().face;

//        FluidStack drainable = handler.drain(side, FluidContainerRegistry.BUCKET_VOLUME, false);
        FluidStack drainable = handler.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (drainable == null || drainable.isEmpty() || !filter.matches(drainable)) {
            return 0;
        }

        drainable = drainable.copy();
//        int filled = robot.fill(null, drainable, doLoad);
        int filled = robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).fill(drainable, doLoad);

        if (filled > 0 && doLoad.execute()) {
            drainable.setAmount(filled);
//            handler.drain(side, drainable, true);
            handler.drain(drainable, IFluidHandler.FluidAction.EXECUTE);
        }
        return filled;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 8 * MjAPI.MJ / 10;
    }
}
