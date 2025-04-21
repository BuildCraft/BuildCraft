/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.*;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.RobotUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class RobotsTriggerProvider implements ITriggerProvider {
    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> result, IStatementContainer container) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        if (stations.size() > 0) {
            result.add(BCRoboticsStatements.triggerRobotSleep);
            result.add(BCRoboticsStatements.triggerRobotInStation);
            result.add(BCRoboticsStatements.triggerRobotLinked);
            result.add(BCRoboticsStatements.triggerRobotReserved);
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @NotNull Direction side) {
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, Direction side, BlockEntity tile) {
    }
}
