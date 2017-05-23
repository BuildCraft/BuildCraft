/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.TriggerWrapper;

public class ElementTrigger extends ElementStatement<TriggerWrapper> {

    public ElementTrigger(GuiGate gui, IGuiArea element, IReference<TriggerWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected TriggerWrapper[] getPossible() {
        TriggerWrapper value = reference.get();
        if (value == null) return null;
        TriggerWrapper[] possible = value.getPossible();
        if (possible == null) return null;

        List<TriggerWrapper> list = new ArrayList<>(possible.length);
        for (TriggerWrapper poss : possible) {
            if (poss.delegate == value.delegate && poss.sourcePart == value.sourcePart) {
                continue;
            }
            if (gui.container.possibleTriggers.contains(poss)) {
                list.add(poss);
            }
        }
        return list.toArray(new TriggerWrapper[list.size()]);
    }
}
