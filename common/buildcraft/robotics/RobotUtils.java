package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.robotics.item.ItemRobot;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class RobotUtils {
    private RobotUtils() {

    }

    public static List<DockingStation> getStations(Object tile) {
        ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

        if (tile instanceof IDockingStationProvider) {
            DockingStation station = ((IDockingStationProvider) tile).getStation();
            if (station != null) {
                stations.add(station);
            }
        }

        if (tile instanceof IPipeHolder) {
            IPipeHolder pipeTile = (IPipeHolder) tile;
            for (Direction d : Direction.values()) {
                if (pipeTile.getPluggable(d) instanceof IDockingStationProvider) {
                    IDockingStationProvider pluggable = (IDockingStationProvider) pipeTile.getPluggable(d);
                    DockingStation station = pluggable.getStation();

                    if (station != null) {
                        stations.add(station);
                    }
                }
            }
        }

        return stations;
    }

    public static RedstoneBoardRobotNBT getNextBoard(ItemStack stack, boolean reverse) {
        Collection<RedstoneBoardNBT<?>> boards = RedstoneBoardRegistry.instance.getAllBoardNBTs();
        if (stack == null || !(stack.getItem() instanceof ItemRobot)) {
            if (!reverse) {
                return (RedstoneBoardRobotNBT) Iterables.getFirst(boards, null);
            } else {
                return (RedstoneBoardRobotNBT) Iterables.getLast(boards, null);
            }
        } else {
            if (reverse) {
                boards = Lists.reverse((List<RedstoneBoardNBT<?>>) boards);
            }
            boolean found = false;
            for (RedstoneBoardNBT<?> boardNBT : boards) {
                if (found) {
                    return (RedstoneBoardRobotNBT) boardNBT;
                } else if (ItemRobot.getRobotNBT(stack) == boardNBT) {
                    found = true;
                }
            }
            return null;
        }
    }
}
