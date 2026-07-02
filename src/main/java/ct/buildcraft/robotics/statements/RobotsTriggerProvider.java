package ct.buildcraft.robotics.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.ITriggerExternal;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.ITriggerInternalSided;
import ct.buildcraft.api.statements.ITriggerProvider;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.robotics.BCRoboticsStatements;
import ct.buildcraft.robotics.plug.RobotStationPluggable;

public class RobotsTriggerProvider implements ITriggerProvider {

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
        if (!(container.getTile() instanceof IPipeHolder holder)) return;

        boolean hasStation = false;
        for (Direction side : Direction.values()) {
            if (holder.getPluggable(side) instanceof RobotStationPluggable) {
                hasStation = true;
                break;
            }
        }
        if (!hasStation) return;

        triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_IN_STATION);
        triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_SLEEP);
        triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_LINKED);
        triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_RESERVED);
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers,
            IStatementContainer container, @Nonnull Direction side) {
        // No sided-only triggers for robots
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers,
            @Nonnull Direction side, BlockEntity tile) {
        // No external triggers
    }
}
