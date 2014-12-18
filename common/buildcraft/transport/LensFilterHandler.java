package buildcraft.transport;

import java.util.HashSet;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeContainer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.pipes.events.PipeEventItem;

/**
* Created by asie on 12/18/14.
*/
public class LensFilterHandler {
	private IPipe pipe;

	public LensFilterHandler(IPipe pipe) {
		this.pipe = pipe;
	}

	public void eventHandler(PipeEventItem.FindDest event) {
		IPipeContainer container = pipe.getTile();
		HashSet<ForgeDirection> wrongColored = new HashSet<ForgeDirection>();
		HashSet<ForgeDirection> anyColored = new HashSet<ForgeDirection>();
		boolean encounteredColor = false;
		int myColor = event.item.color == null ? -1 : event.item.color.ordinal();

		for (ForgeDirection dir: event.destinations) {
			int sideColor = -1;

			// Get the side's color
			// (1/2) From this pipe's outpost
			PipePluggable pluggable = container.getPipePluggable(dir);
			if (pluggable != null && pluggable instanceof LensPluggable && ((LensPluggable) pluggable).isFilter) {
				sideColor = ((LensPluggable) pluggable).color;
			}

			// (2/2) From the other pipe's outpost
			IPipe otherPipe = container.getNeighborPipe(dir);
			if (otherPipe != null && otherPipe.getTile() != null) {
				IPipeContainer otherContainer = otherPipe.getTile();
				pluggable = otherContainer.getPipePluggable(dir.getOpposite());
				if (pluggable != null && pluggable instanceof LensPluggable && ((LensPluggable) pluggable).isFilter) {
					int otherColor = ((LensPluggable) pluggable).color;
					// Check if colors conflict - if so, the side is unpassable
					if (sideColor >= 0 && otherColor != sideColor) {
						wrongColored.add(dir);
						continue;
					} else {
						sideColor = otherColor;
					}
				}
			}

			if (myColor == sideColor) {
				encounteredColor = true;
			} else {
				wrongColored.add(dir);
			}

			if (sideColor != -1) {
				anyColored.add(dir);
			}
		}

		for (ForgeDirection dir : (encounteredColor ? wrongColored : anyColored)) {
			event.destinations.remove(dir);
		}
	}
}
