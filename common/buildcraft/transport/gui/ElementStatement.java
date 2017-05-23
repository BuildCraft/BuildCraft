/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import java.util.List;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.ActionWrapper;
import buildcraft.transport.gate.StatementWrapper;
import buildcraft.transport.gate.TriggerWrapper;

public abstract class ElementStatement<T extends StatementWrapper> extends ElementGuiSlot<T> {

    public ElementStatement(GuiGate gui, IGuiArea element, IReference<T> reference) {
        super(gui, element, reference);
    }

    public boolean hasParam(int param) {
        T statement = reference.get();
        if (statement == null) {
            return false;
        } else {
            return param < statement.maxParameters();
        }
    }

    @Override
    public void onMouseClicked(int button) {
        if (button == 1 && contains(gui.mouse)) {// right click
            if (!gui.isDraggingStatement) {
                T value = reference.get();
                if (value != null) {
                    // Copy trigger/action on right click
                    if (value instanceof TriggerWrapper) {
                        gui.draggingTrigger = (TriggerWrapper) value;
                        gui.isDraggingStatement = true;
                    } else if (value instanceof ActionWrapper) {
                        gui.draggingAction = (ActionWrapper) value;
                        gui.isDraggingStatement = true;
                    }
                }
            }
        } else {
            super.onMouseClicked(button);
        }
    }

    @Override
    public void draw(T statement, IGuiPosition pos) {
        draw(gui, statement, pos);
    }

    @Override
    protected void addToolTip(T value, List<ToolTip> tooltips) {
        String[] arr = new String[0];
        String desc = value.getDescription();
        if (desc != null && desc.length() > 0) {
            arr = new String[] { desc };
        } else {
            return;
        }
        EnumFacing face = value.sourcePart.face;
        if (face != null) {
            String translated = ColourUtil.getTextFullTooltip(face);
            translated = LocaleUtil.localize("gate.side", translated);
            arr = new String[] { arr[0], translated };
        }
        tooltips.add(new ToolTip(arr));
    }

    public static void draw(GuiGate gui, StatementWrapper statement, IGuiPosition pos) {
        if (statement == null) {
            GuiGate.SLOT_COLOUR.drawAt(pos);
            return;
        }
        EnumPipePart part = statement.sourcePart;
        int yOffset = (part.getIndex() + 1) % 7;
        GuiGate.SLOT_COLOUR.offset(0, yOffset * 18).drawAt(pos);
        ElementGuiSlot.draw(gui, statement, pos);
    }
}
