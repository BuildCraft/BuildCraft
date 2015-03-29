package buildcraft.robotics.statements;

import java.util.Iterator;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.transport.IPipeTile;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.RobotUtils;

public class DockingStationIterator implements Iterator<DockingStation> {
	private final IPipeTile tile;
	private DockingStation next;
	private int side = -1;

	public DockingStationIterator(IStatementContainer container) {
		tile = container.getTile() instanceof IPipeTile ? (IPipeTile) container.getTile() : null;
		findNext();
	}

	private void findNext() {
		while (side < 6) {
			side++;
			DockingStation station = RobotUtils.getStation(tile, ForgeDirection.getOrientation(side));

			if (station != null) {
				next = station;
				return;
			}
		}
		next = null;
	}

	@Override
	public boolean hasNext() {
		return tile != null && next != null;
	}

	@Override
	public DockingStation next() {
		if (hasNext()) {
			DockingStation output = next;
			findNext();
			return output;
		} else {
			return null;
		}
	}

	@Override
	public void remove() {
	}
}
