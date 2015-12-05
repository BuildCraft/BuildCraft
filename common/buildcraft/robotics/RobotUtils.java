package buildcraft.robotics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.transport.IPipeTile;

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

		if (tile instanceof IPipeTile) {
			IPipeTile pipeTile = (IPipeTile) tile;
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				if (pipeTile.getPipePluggable(d) instanceof IDockingStationProvider) {
					IDockingStationProvider pluggable = (IDockingStationProvider) pipeTile.getPipePluggable(d);
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
