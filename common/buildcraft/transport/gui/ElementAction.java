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

import buildcraft.transport.gate.ActionWrapper;

public class ElementAction extends ElementStatement<ActionWrapper> {

    public ElementAction(GuiGate gui, IGuiArea element, IReference<ActionWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected ActionWrapper[] getPossible() {
        ActionWrapper value = reference.get();
        if (value == null) return null;
        ActionWrapper[] possible = value.getPossible();
        if (possible == null) return null;

        List<ActionWrapper> list = new ArrayList<>(possible.length);
        for (ActionWrapper poss : possible) {
            if (poss.delegate == value.delegate && poss.sourcePart == value.sourcePart) {
                continue;
            }
            if (gui.container.possibleActions.contains(poss)) {
                list.add(poss);
            }
        }
        return list.toArray(new ActionWrapper[list.size()]);
    }
}
