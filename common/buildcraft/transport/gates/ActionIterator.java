/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gates;

import java.util.Iterator;
import java.util.List;

import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipe;

public class ActionIterator implements Iterable<StatementSlot> {
    private IPipe pipe;

    public ActionIterator(IPipe iPipe) {
        pipe = iPipe;
    }

    @Override
    public Iterator<StatementSlot> iterator() {
        return new It();
    }

    private class It implements Iterator<StatementSlot> {

        private EnumFacing curDir = EnumFacing.values()[0];
        private int index = 0;

        @Override
        public boolean hasNext() {
            return getNext(false) != null;
        }

        @Override
        public StatementSlot next() {
            return getNext(true);
        }

        private StatementSlot getNext(boolean advance) {
            EnumFacing curDir = this.curDir;
            int index = this.index;
            while (true) {
                List<StatementSlot> lst = pipe.hasGate(curDir) ? pipe.getGate(curDir).getActiveActions() : null;
                if (lst == null || index >= lst.size()) {
                    if (curDir.ordinal() == 5) {
                        return null;
                    }
                    curDir = EnumFacing.values()[curDir.ordinal() + 1];
                } else {
                    index++;
                    if (advance) {
                        this.curDir = curDir;
                        this.index = index;
                    }
                    return lst.get(index - 1);
                }
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported.");
        }
    }
}
