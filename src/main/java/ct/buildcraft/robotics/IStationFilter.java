package ct.buildcraft.robotics;

import ct.buildcraft.api.robots.DockingStation;

@FunctionalInterface
public interface IStationFilter {
    boolean matches(DockingStation station);
}
