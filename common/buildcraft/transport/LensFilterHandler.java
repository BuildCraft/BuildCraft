package buildcraft.transport;

import java.util.LinkedList;

import net.minecraftforge.common.util.ForgeDirection;

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
		LinkedList<ForgeDirection> correctColored = new LinkedList<ForgeDirection>();
		LinkedList<ForgeDirection> notColored = new LinkedList<ForgeDirection>();
		boolean encounteredColor = false;
		int myColor = event.item.color == null ? -1 : event.item.color.ordinal();

		for (ForgeDirection dir : event.destinations) {
			boolean hasFilter = false;
			boolean hasLens = false;
			int sideColor = -1;
			int sideLensColor = -1;

			// Get the side's color
			// (1/2) From this pipe's outpost
			PipePluggable pluggable = container.getPipePluggable(dir);
			if (pluggable != null && pluggable instanceof LensPluggable) {
				if (((LensPluggable) pluggable).isFilter) {
					hasFilter = true;
					sideColor = ((LensPluggable) pluggable).color;
				} else {
					hasLens = true;
					sideLensColor = ((LensPluggable) pluggable).color;
				}
			}

			// (2/2) From the other pipe's outpost
			IPipe otherPipe = container.getNeighborPipe(dir);
			if (otherPipe != null && otherPipe.getTile() != null) {
				IPipeTile otherContainer = otherPipe.getTile();
				pluggable = otherContainer.getPipePluggable(dir.getOpposite());
				if (pluggable != null && pluggable instanceof LensPluggable && ((LensPluggable) pluggable).isFilter) {
					int otherColor = ((LensPluggable) pluggable).color;
					if (hasFilter && otherColor != sideColor) {
						// Filter colors conflict - the side is unpassable
						continue;
					} else if (hasLens) {
						// The closer lens color differs from the further away filter color - the side is unpassable OR treated as colorless
						if (sideLensColor == otherColor) {
							hasFilter = false;
							sideColor = -1;
						} else {
							continue;
						}
					} else {
						hasFilter = true;
						sideColor = otherColor;
					}
				}
			}

			if (hasFilter) {
				if (myColor == sideColor) {
					encounteredColor = true;
					correctColored.add(dir);
				}
			} else {
				notColored.add(dir);
			}
		}

		event.destinations.clear();
		event.destinations.addAll(encounteredColor ? correctColored : notColored);
	}
}
