package ct.buildcraft.api.robots;

/** By default, this can be either a pipe pluggable or a block entity. */
public interface IDockingStationProvider {
    DockingStation getStation();
}
