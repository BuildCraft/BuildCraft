package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.IZone;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.IStationFilter;
import ct.buildcraft.robotics.statements.ActionStationForbidRobot;
import net.minecraft.world.phys.Vec3;

public class AIRobotSearchStation extends AIRobot {
    public DockingStation targetStation;
    private IStationFilter filter;
    private IZone zone;

    public AIRobotSearchStation(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotSearchStation(EntityRobotBase robot, IStationFilter filter, IZone zone) {
        this(robot);
        this.filter = filter;
        this.zone = zone;
    }

    @Override
    public void start() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }
        if (robot.getDockingStation() != null && filter.matches(robot.getDockingStation())) {
            targetStation = robot.getDockingStation();
            terminate();
            return;
        }
        double bestDistance = Double.MAX_VALUE;
        for (DockingStation station : robot.getRegistry().getStations()) {
            if (station == null || !station.isInitialized()) continue;
            if (station.isTaken() && station.robotIdTaking() != robot.getRobotId()) continue;
            if (zone != null && !zone.contains(new Vec3(station.x(), station.y(), station.z()))) continue;
            if (!filter.matches(station)) continue;
            if (ActionStationForbidRobot.isForbidden(station, robot)) continue;
            double dx = robot.getX() - station.x();
            double dy = robot.getY() - station.y();
            double dz = robot.getZ() - station.z();
            double distance = dx * dx + dy * dy + dz * dz;
            if (targetStation == null || distance < bestDistance) {
                targetStation = station;
                bestDistance = distance;
            }
        }
        terminate();
    }

    @Override
    public boolean success() {
        return targetStation != null;
    }
}
