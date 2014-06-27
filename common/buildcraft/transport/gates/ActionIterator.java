/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import java.util.Iterator;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.transport.ActionActiveState;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;

public class ActionIterator implements Iterable<ActionSlot> {

	private Pipe pipe;

	public ActionIterator(Pipe iPipe) {
		pipe = iPipe;
	}

	@Override
	public Iterator<ActionSlot> iterator() {
		return new It();
	}

	private class It implements Iterator<ActionSlot> {

		private ForgeDirection curDir = ForgeDirection.values()[0];
		private int index = 0;
		private ActionSlot next;

		public It() {
			while (!isValid()) {
				if (index < Gate.MAX_STATEMENTS - 1) {
					index++;
				} else if (curDir != ForgeDirection.UNKNOWN) {
					index = 0;
					curDir = ForgeDirection.values()[curDir.ordinal() + 1];
				} else {
					break;
				}
			}

			if (isValid()) {
				next = new ActionSlot();
				next.action = pipe.gates[curDir.ordinal()].actions[index];
				next.parameters = pipe.gates[curDir.ordinal()].actionParameters[index];
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public ActionSlot next() {
			ActionSlot result = next;

			while (true) {
				if (index < Gate.MAX_STATEMENTS - 1) {
					index++;
				} else if (curDir != ForgeDirection.UNKNOWN) {
					index = 0;
					curDir = ForgeDirection.values()[curDir.ordinal() + 1];
				} else {
					break;
				}

				if (isValid()) {
					break;
				}
			}

			if (isValid()) {
				next = new ActionSlot();
				next.action = pipe.gates[curDir.ordinal()].actions[index];
				next.parameters = pipe.gates[curDir.ordinal()].actionParameters[index];
			} else {
				next = null;
			}

			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported.");
		}

		private boolean isValid() {
			if (curDir == ForgeDirection.UNKNOWN) {
				return false;
			}

			Gate gate = pipe.gates[curDir.ordinal()];

			if (gate == null) {
				return false;
			}

			if (gate.actions[index] == null || gate.actionsState[index] != ActionActiveState.Activated) {
				return false;
			}

			return true;
		}
	};
}
