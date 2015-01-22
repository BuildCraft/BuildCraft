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

import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;

public class ActionIterator implements Iterable<StatementSlot> {

	private Pipe pipe;

	public ActionIterator(Pipe iPipe) {
		pipe = iPipe;
	}

	@Override
	public Iterator<StatementSlot> iterator() {
		return new It();
	}

	private class It implements Iterator<StatementSlot> {

		private ForgeDirection curDir = ForgeDirection.values()[0];
		private int index = 0;
		private StatementSlot next;

		public It() {
			while (!isValid()) {
				if (curDir == ForgeDirection.UNKNOWN) {
					break;
				} else if (pipe.gates[curDir.ordinal()] == null
						|| index >= pipe.gates[curDir.ordinal()].activeActions.size() - 1) {
					index = 0;
					curDir = ForgeDirection.values()[curDir.ordinal() + 1];
				} else {
					index++;
				}
			}

			if (isValid()) {
				next = pipe.gates[curDir.ordinal()].activeActions.get(index);
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public StatementSlot next() {
			StatementSlot result = next;

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
				next = pipe.gates[curDir.ordinal()].activeActions.get(index);
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
			return curDir != ForgeDirection.UNKNOWN
					&& pipe.gates[curDir.ordinal()] != null
					&& index < pipe.gates[curDir.ordinal()].activeActions.size();
		}
	}
}
