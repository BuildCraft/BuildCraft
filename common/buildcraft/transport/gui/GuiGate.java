/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import java.io.IOException;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatementContainer;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.ElementStatementParam;
import buildcraft.lib.gui.statement.GuiStatementSelector;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;

import buildcraft.transport.container.ContainerGate;
import buildcraft.transport.gate.ActionWrapper;
import buildcraft.transport.gate.GateLogic;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.gate.TriggerWrapper;

public class GuiGate extends GuiStatementSelector<ContainerGate> {

    public static final ResourceLocation TEXTURE_GATE = new ResourceLocation("buildcrafttransport:textures/gui/gate_interface.png");

    public static final int GUI_HEIGHT_NORM = 117;

    public static final GuiIcon ICON_BACK_TOP = new GuiIcon(TEXTURE_GATE, 0, 0, GUI_WIDTH, 16);
    public static final GuiIcon ICON_BACK_MID = new GuiIcon(TEXTURE_GATE, 0, 23, GUI_WIDTH, 18);
    public static final GuiIcon ICON_BACK_BOTTOM = new GuiIcon(TEXTURE_GATE, 0, 48, GUI_WIDTH, 101);

    public static final GuiIcon CONNECT_HORIZ_OFF = new GuiIcon(TEXTURE_GATE, 176, 18, 18, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_TRIGGER = new GuiIcon(TEXTURE_GATE, 194, 18, 7, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_ACTION = new GuiIcon(TEXTURE_GATE, 201, 18, 11, 18);
    public static final GuiIcon CONNECT_HORIZ_ON_FULL = CONNECT_HORIZ_OFF.offset(18, 0);

    public static final GuiIcon CONNECT_VERT_TOP_OFF = new GuiIcon(TEXTURE_GATE, 176, 36, 18, 9);
    public static final GuiIcon CONNECT_VERT_TOP_ON = CONNECT_VERT_TOP_OFF.offset(18, 0);

    public static final GuiIcon CONNECT_VERT_BOTTOM_OFF = CONNECT_VERT_TOP_OFF.offset(0, 9);
    public static final GuiIcon CONNECT_VERT_BOTTOM_ON = CONNECT_VERT_TOP_ON.offset(0, 9);

    public static final GuiIcon CONNECT_VERT_OFF = new GuiIcon(TEXTURE_GATE, 176, 54, 18, 18);
    public static final GuiIcon CONNECT_VERT_ON = CONNECT_VERT_OFF.offset(18, 0);

    private final IGuiArea[] positionSlotPair, positionConnect;

    public GuiGate(ContainerGate container) {
        super(container);

        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT_NORM + 18 * container.slotHeight;

        boolean split = container.gate.isSplitInTwo();
        GateVariant variant = container.gate.variant;
        int triggerWidth = 18 * (variant.numTriggerArgs + 1);
        int columnWidth = 18 * 2 + triggerWidth + 18 * variant.numActionArgs;

        int columnStartFirst = split ? (GUI_WIDTH - 18) / 2 - columnWidth : (GUI_WIDTH - columnWidth) / 2;
        int columnStartSecond = columnStartFirst + columnWidth + 18;

        int numSlots = variant.numSlots;
        positionSlotPair = new IGuiArea[numSlots];
        positionConnect = new IGuiArea[numSlots];

        for (int i = 0; i < numSlots; i++) {
            boolean otherColumn = split && i >= container.slotHeight;
            int x = otherColumn ? columnStartSecond : columnStartFirst;
            int y = (otherColumn ? i - container.slotHeight : i) * 18 + 16;
            positionSlotPair[i] = new GuiRectangle(x, y, columnWidth, 18).offset(rootElement);

            boolean nextOtherColumn = split && i + 1 >= container.slotHeight;
            if (otherColumn == nextOtherColumn && i < numSlots - 1) {
                positionConnect[i] = new GuiRectangle(x + triggerWidth, y + 9, 18, 18).offset(rootElement);
            }
        }

        MessageUtil.doDelayed(() -> {
            container.sendMessage(ContainerGate.ID_VALID_STATEMENTS);
            container.sendMessage(ContainerGate.ID_CURRENT_SET);
        });
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        return false;
    }

    @Override
    public IStatementContainer getStatementContainer() {
        return container.gate;
    }

    @Override
    public void initGui() {
        super.initGui();

        GateVariant variant = container.gate.variant;
        int numSlots = variant.numSlots;

        for (int i = 0; i < numSlots; i++) {
            IGuiArea actionPos = positionSlotPair[i].offset(18 * (2 + variant.numTriggerArgs), 0);
            ElementTrigger trigger = new ElementTrigger(this, positionSlotPair[i].resize(18, 18), container.pairs[i].trigger);
            ElementAction action = new ElementAction(this, actionPos.resize(18, 18), container.pairs[i].action);
            shownElements.add(trigger);
            shownElements.add(action);
            for (int p = 0; p < variant.numTriggerArgs; p++) {
                IGuiArea pos = positionSlotPair[i].offset(18 * (p + 1), 0).resize(18, 18);
                shownElements.add(new ElementStatementParam(this, pos, container.pairs[i].triggerParams[p], p, trigger));
            }
            for (int p = 0; p < variant.numActionArgs; p++) {
                IGuiArea pos = actionPos.offset(18 * (p + 1), 0).resize(18, 18);
                shownElements.add(new ElementStatementParam(this, pos, container.pairs[i].actionParams[p], p, action));
            }
        }
    }

    // Drawing

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_BACK_TOP.drawAt(rootElement);
        int y = 16;
        for (int i = 0; i < container.slotHeight; i++) {
            ICON_BACK_MID.drawAt(rootElement.offset(0, y));
            y += 18;
        }
        ICON_BACK_BOTTOM.drawAt(rootElement.offset(0, y));

        GateLogic gate = container.gate;
        int triggerArgs = gate.variant.numTriggerArgs;

        for (int i = 0; i < positionSlotPair.length; i++) {
            int xOff = 18 * (triggerArgs + 1);
            IGuiArea elemSlots = positionSlotPair[i];

            IGuiArea offsetConnect = elemSlots.offset(xOff, 0);

            boolean triggerOn = gate.triggerOn[i];
            boolean actionOn = gate.actionOn[i];

            if (triggerOn) {
                if (actionOn) {
                    CONNECT_HORIZ_ON_FULL.drawAt(offsetConnect);
                } else {
                    CONNECT_HORIZ_OFF.drawAt(offsetConnect);
                    CONNECT_HORIZ_ON_TRIGGER.drawAt(offsetConnect);
                }
            } else if (actionOn) {
                CONNECT_HORIZ_OFF.drawAt(offsetConnect);
                CONNECT_HORIZ_ON_ACTION.drawAt(offsetConnect.getX() + 7, offsetConnect.getY());
            } else {
                CONNECT_HORIZ_OFF.drawAt(offsetConnect);
            }

            IGuiArea elemConnect = positionConnect[i];
            if (elemConnect != null) {
                boolean twoConnected = gate.connections[i];
                boolean bottomOn = gate.actionOn[i + 1];
                if (twoConnected) {
                    GuiIcon icon = actionOn ? CONNECT_VERT_ON : CONNECT_VERT_OFF;
                    icon.drawAt(elemConnect);
                } else {
                    GuiIcon icon = actionOn ? CONNECT_VERT_TOP_ON : CONNECT_VERT_TOP_OFF;
                    icon.drawAt(elemConnect);
                    icon = bottomOn ? CONNECT_VERT_BOTTOM_ON : CONNECT_VERT_BOTTOM_OFF;
                    icon.drawAt(elemConnect.offset(0, 9));
                }
            }
        }
        super.drawBackgroundLayer(partialTicks);
    }

    @Override
    protected void drawForegroundLayer() {
        double x = rootElement.getX();
        double y = rootElement.getY();
        String localizedName = container.gate.variant.getLocalizedName();
        double cX = x + (GUI_WIDTH - fontRenderer.getStringWidth(localizedName)) / 2;
        fontRenderer.drawString(localizedName, (int) cX, (int) y + 5, 0x404040);
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) x + 8, (int) y + ySize - 97, 0x404040);
        GlStateManager.color(1, 1, 1);
        super.drawForegroundLayer();
    }

    @Override
    protected void iteratePossible(OnStatement consumer) {
        int tx = 0;
        int ty = 0;
        EnumPipePart last = null;
        consumer.iterate(null, rootElement.offset(-18, 8).resize(18, 18));
        for (TriggerWrapper wrapper : container.possibleTriggers) {
            tx++;
            if (tx > 3 || (last != null && last != wrapper.sourcePart)) {
                tx = 0;
                ty++;
            }
            consumer.iterate(wrapper, rootElement.offset(18 * (-1 - tx), ty * 18 + 8).resize(18, 18));
            last = wrapper.sourcePart;
        }

        tx = 0;
        ty = 0;
        last = null;
        consumer.iterate(null, rootElement.offset(GUI_WIDTH, 8).resize(18, 18));
        for (ActionWrapper wrapper : container.possibleActions) {
            tx++;
            if (tx > 3 || (last != null && last != wrapper.sourcePart)) {
                tx = 0;
                ty++;
            }
            consumer.iterate(wrapper, rootElement.offset(GUI_WIDTH + 18 * tx, ty * 18 + 8).resize(18, 18));
            last = wrapper.sourcePart;
        }
    }

    // Mouse logic

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Test for connection changes
        for (int i = 0; i < positionConnect.length; i++) {
            IGuiArea pos = positionConnect[i];
            if (pos != null && pos.contains(mouse)) {
                container.setConnected(i, !container.gate.connections[i]);
                return;
            }
        }
    }
}
