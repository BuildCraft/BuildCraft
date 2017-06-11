/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.StatementWrapper;

public abstract class ElementStatement<G extends GuiStatementSelector<?>, T extends StatementWrapper> extends ElementGuiSlot<G, T> {

    public ElementStatement(G gui, IGuiArea element, IReference<T> reference) {
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
            if (gui.draggingElement == null) {
                gui.draggingElement = reference.get();
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

    public static void draw(Gui gui, StatementWrapper statement, IGuiPosition pos) {
        if (statement == null) {
            GuiStatementSelector.SLOT_COLOUR.drawAt(pos);
            return;
        }
        EnumPipePart part = statement.sourcePart;
        int yOffset = (part.getIndex() + 1) % 7;
        GuiStatementSelector.SLOT_COLOUR.offset(0, yOffset * 18).drawAt(pos);
        ElementGuiSlot.draw(gui, statement, pos);
    }
}
