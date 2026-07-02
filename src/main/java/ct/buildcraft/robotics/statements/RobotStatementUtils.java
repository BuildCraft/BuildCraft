package ct.buildcraft.robotics.statements;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.robotics.plug.RobotStationPluggable;

public final class RobotStatementUtils {
    private RobotStatementUtils() {}

    /** Returns the first docking station found on the pipe that holds the given container. */
    @Nullable
    public static DockingStation getStation(IStatementContainer container) {
        List<DockingStation> stations = getStations(container);
        return stations.isEmpty() ? null : stations.get(0);
    }

    /** Returns all docking stations found on the pipe that holds the given container. */
    public static List<DockingStation> getStations(IStatementContainer container) {
        List<DockingStation> result = new ArrayList<>();
        BlockEntity te = container.getTile();
        if (!(te instanceof IPipeHolder holder)) return result;
        for (Direction side : Direction.values()) {
            if (holder.getPluggable(side) instanceof RobotStationPluggable plug) {
                DockingStation st = plug.getStation();
                if (st != null) result.add(st);
            }
        }
        return result;
    }
}
