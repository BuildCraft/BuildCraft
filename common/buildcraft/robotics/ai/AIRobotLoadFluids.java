/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationProvideFluids;

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
            if (load(robot, robot.getDockingStation(), filter, true) == 0) {
                terminate();
            } else {
                setSuccess(true);
                waitedCycles = 0;
            }
        }
    }

    public static int load(EntityRobotBase robot, DockingStation station, IFluidFilter filter, boolean doLoad) {
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

        EnumFacing side = station.getFluidInputSide().face;

        FluidStack drainable = handler.drain(side, FluidContainerRegistry.BUCKET_VOLUME, false);
        if (drainable == null || !filter.matches(drainable.getFluid())) {
            return 0;
        }

        drainable = drainable.copy();
        int filled = robot.fill(null, drainable, doLoad);

        if (filled > 0 && doLoad) {
            drainable.amount = filled;
            handler.drain(side, drainable, true);
        }
        return filled;
    }

    @Override
    public int getEnergyCost() {
        return 8;
    }

}
