/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders;

import java.util.Iterator;
import java.util.LinkedList;

public class BuildingSlotIterator {
	private static int ITERATIONS_MAX = 500;

	private final LinkedList<BuildingSlotBlock> buildList;
	private Iterator<BuildingSlotBlock> current;
	private int nbIterations;

	/**
	 * Creates an iterator on the list, which will cycle through iterations per
	 * chunk.
	 */
	public BuildingSlotIterator(LinkedList<BuildingSlotBlock> buildList) {
		this.buildList = buildList;
	}

	public void startIteration() {
		if (current == null || !current.hasNext()) {
			current = buildList.iterator();
		}

		nbIterations = 0;
	}

	public boolean hasNext() {
		return current.hasNext() && nbIterations < ITERATIONS_MAX;
	}

	public BuildingSlotBlock next() {
		BuildingSlotBlock next = current.next();

		if (next == null) {
			// we're only accepting to pass through a null element if this is
			// the first iteration. Otherwise, elements before null need to
			// be worked out.
			if (nbIterations == 0) {
				current.remove();
			}
		}

		nbIterations++;
		return next;
	}

	public void remove() {
		current.remove();
	}

	public void reset() {
		current = buildList.iterator();
		nbIterations = 0;
	}

}
