/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.*;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.RobotUtils;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class RobotsActionProvider implements IActionProvider {

    @Override
    public void addInternalActions(Collection<IActionInternal> result, IStatementContainer container) {
        BlockEntity tile = container.getTile();

        if (!(tile instanceof IPipeHolder)) {
            return;
        }

        IPipeHolder pipeTile = (IPipeHolder) tile;

        List<DockingStation> stations = RobotUtils.getStations(pipeTile);

        if (stations.size() == 0) {
            return;
        }

        result.add(BCRoboticsStatements.actionRobotGotoStation);
        result.add(BCRoboticsStatements.actionRobotWorkInArea);
        result.add(BCRoboticsStatements.actionRobotLoadUnloadArea);
        result.add(BCRoboticsStatements.actionRobotWakeUp);
        result.add(BCRoboticsStatements.actionRobotFilter);
        result.add(BCRoboticsStatements.actionRobotFilterTool);
        result.add(BCRoboticsStatements.actionStationForbidRobot);
        result.add(BCRoboticsStatements.actionStationForceRobot);

        if (pipeTile.getPipe().getFlow() instanceof PipeFlowItems) {
            result.add(BCRoboticsStatements.actionStationRequestItems);
            result.add(BCRoboticsStatements.actionStationAcceptItems);
        }

        if (pipeTile.getPipe().getFlow() instanceof PipeFlowFluids) {
            result.add(BCRoboticsStatements.actionStationAcceptFluids);
        }

        for (DockingStation station : stations) {
            if (station.getItemInput() != null) {
                result.add(BCRoboticsStatements.actionStationProvideItems);
            }

            if (station.getFluidInput() != null) {
                result.add(BCRoboticsStatements.actionStationProvideFluids);
            }

            if (station.getRequestProvider() != null) {
                result.add(BCRoboticsStatements.actionStationMachineRequestItems);
            }
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @NotNull Direction side) {
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, Direction side, BlockEntity tile) {
    }

}
