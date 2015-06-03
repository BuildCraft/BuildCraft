package buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.pipes.events.PipeEventPriority;
import buildcraft.transport.pluggable.LensPluggable;

public class LensFilterHandler {
	@PipeEventPriority(priority = -100)
	public void eventHandler(PipeEventItem.FindDest event) {
		IPipeTile container = event.pipe.getTile();
		LinkedList<EnumFacing> correctColored = new LinkedList<EnumFacing>();
		LinkedList<EnumFacing> notColored = new LinkedList<EnumFacing>();
		boolean encounteredColor = false;
		int myColor = event.item.color == null ? -1 : event.item.color.ordinal();

		for (EnumFacing dir: event.destinations) {
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
				IPipeTile otherContainer = otherPipe.getTile();
				pluggable = otherContainer.getPipePluggable(dir.getOpposite());
				if (pluggable != null && pluggable instanceof LensPluggable && ((LensPluggable) pluggable).isFilter) {
					int otherColor = ((LensPluggable) pluggable).color;
					// Check if colors conflict - if so, the side is unpassable
					if (sideColor >= 0 && otherColor != sideColor) {
						continue;
					} else {
						sideColor = otherColor;
					}
				}
			}

			if (myColor == sideColor) {
				encounteredColor = true;
				correctColored.add(dir);
			}

			if (sideColor == -1) {
				notColored.add(dir);
			}
		}

		event.destinations.clear();
		event.destinations.addAll(encounteredColor ? correctColored : notColored);
	}
}
