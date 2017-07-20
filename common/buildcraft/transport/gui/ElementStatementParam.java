/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.data.IReference;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class ElementStatementParam extends ElementGuiSlot<IStatementParameter> {
    public final int paramIndex;
    public final ElementStatement<?> parent;

    public ElementStatementParam(GuiGate gui, IGuiArea element, IReference<IStatementParameter> reference, int paramIndex, ElementStatement<?> elemParent) {
        super(gui, element, reference);
        this.paramIndex = paramIndex;
        this.parent = elemParent;
    }

    @Override
    public void drawBackground(float partialTicks) {
        IStatementParameter param = reference.get();
        if (!parent.hasParam(paramIndex)) {
            GuiGate.ICON_SLOT_BLOCKED.drawAt(this);
        } else if (param == null) {
            GuiGate.ICON_SLOT_NOT_SET.drawAt(this);
        } else {
            GuiGate.SLOT_COLOUR.drawAt(this);
            super.drawBackground(partialTicks);
            ItemStack stack = param.getItemStack();
            if (!stack.isEmpty()) {
                gui.drawItemStackAt(stack, getX() + 1, getY() + 1);
            }
        }
    }

    @Override
    public void draw(IStatementParameter val, IGuiPosition element) {
        if (val == null) {
            GuiGate.ICON_SLOT_BLOCKED.drawAt(element);
        } else {
            GuiGate.SLOT_COLOUR.drawAt(element);
            super.draw(val, element);
            ItemStack stack = val.getItemStack();
            if (!stack.isEmpty()) {
                gui.drawItemStackAt(stack, getX() + 1, getY() + 1);
            }
        }
    }

    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            IStatementParameter value = reference.get();
            if (value == null) return;
            if (value.onClick(gui.container.gate, parent.reference.get(), gui.mc.player.inventory.getItemStack(), new StatementMouseClick(button, GuiScreen.isShiftKeyDown()))) {
                // update the server with the click
                reference.set(value);
                return;
            }
            displayPossible();
        }
    }

    @Override
    protected IStatementParameter[] getPossible() {
        IStatementParameter value = reference.get();
        if (value == null) return null;
        return value.getPossible(gui.container.gate, parent.reference.get());
    }
}
