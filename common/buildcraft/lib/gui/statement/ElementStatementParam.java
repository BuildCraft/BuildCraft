/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.gui.statement;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.data.IReference;

public class ElementStatementParam extends ElementGuiSlot<GuiStatementSelector<?>, IStatementParameter> {
    public final int paramIndex;
    public final ElementStatement<?, ?> parent;

    public ElementStatementParam(GuiStatementSelector<?> gui, IGuiArea element, IReference<IStatementParameter> reference, int paramIndex, ElementStatement<?, ?> elemParent) {
        super(gui, element, reference);
        this.paramIndex = paramIndex;
        this.parent = elemParent;
    }

    @Override
    public void drawBackground(float partialTicks) {
        IStatementParameter param = reference.get();
        if (!parent.hasParam(paramIndex)) {
            GuiStatementSelector.ICON_SLOT_BLOCKED.drawAt(this);
        } else if (param == null) {
            GuiStatementSelector.ICON_SLOT_NOT_SET.drawAt(this);
        } else {
            GuiStatementSelector.SLOT_COLOUR.drawAt(this);
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
            GuiStatementSelector.ICON_SLOT_BLOCKED.drawAt(element);
        } else {
            GuiStatementSelector.SLOT_COLOUR.drawAt(element);
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
            if (value == null) {
                return;
            }
            ItemStack currentStack = gui.mc.player.inventory.getItemStack();
            StatementMouseClick event = new StatementMouseClick(button, GuiScreen.isShiftKeyDown());
            IStatementParameter newParam = value.onClick(gui.getStatementContainer(), parent.reference.get(), currentStack, event);
            if (newParam != null) {
                // update the server with the click
                reference.set(newParam);
                return;
            }
            displayPossible();
        }
    }

    @Override
    protected IStatementParameter[] getPossible() {
        IStatementParameter value = reference.get();
        if (value == null) {
            return null;
        }
        return value.getPossible(gui.getStatementContainer(), parent.reference.get());
    }
}
