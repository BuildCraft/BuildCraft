/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.inventory.filter.SimpleFluidFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAcceptFluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class AIRobotUnloadFluids extends AIRobot {

    private int waitedCycles = 0;

    public AIRobotUnloadFluids(EntityRobotBase iRobot) {
        super(iRobot);
        setSuccess(false);
    }

    @Override
    public void update() {
        waitedCycles++;

        if (waitedCycles > 40) {
            if (unload(robot, robot.getDockingStation(), IFluidHandler.FluidAction.EXECUTE) == 0) {
                terminate();
            } else {
                setSuccess(true);
            }
        }
    }

    // public static int unload(EntityRobotBase robot, DockingStation station, boolean doUnload)
    public static int unload(EntityRobotBase robot, DockingStation station, IFluidHandler.FluidAction doUnload) {
        if (station == null) {
            return 0;
        }

        if (!ActionRobotFilter.canInteractWithFluid(station, new SimpleFluidFilter(robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).getFluidInTank(0)),
                ActionStationAcceptFluids.class)) {
            return 0;
        }

        IFluidHandler fluidHandler = station.getFluidOutput();
        if (fluidHandler == null) {
            return 0;
        }

//        FluidStack drainable = robot.drain(null, FluidAttributes.BUCKET_VOLUME, false);
        FluidStack drainable = robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
//        if (drainable == null)
        if (drainable == null || drainable.isEmpty()) {
            return 0;
        }

        drainable = drainable.copy();
//        int filled = fluidHandler.fill(station.getFluidOutputSide().face, drainable, doUnload);
        int filled = fluidHandler.fill(drainable, doUnload);

        if (filled > 0 && doUnload.execute()) {
            drainable.setAmount(filled);
//            robot.drain(null, drainable, true);
            robot.getCapability(CapUtil.CAP_FLUIDS).orElse(null).drain(drainable, IFluidHandler.FluidAction.EXECUTE);
        }
        return filled;
    }

    @Override
    public long getPowerCost() {
        return 10 * MjAPI.MJ / 10;
    }
}
