/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

public class WidgetFluidTank extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLICK = 0;

    private final Tank tank;

    public WidgetFluidTank(ContainerBC_Neptune container, Tank tank) {
        super(container);
        this.tank = tank;
    }

    @Override
    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            tank.onGuiClicked(container);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IGuiElement createGuiElement(GuiBC8<?> gui, IGuiPosition parent, GuiRectangle position, GuiIcon overlay) {
        return new GuiElementFluidTank(gui, parent, position, overlay);
    }

    private final class GuiElementFluidTank extends GuiElementSimple<GuiBC8<?>> {
        private final GuiIcon overlay;

        public GuiElementFluidTank(GuiBC8<?> gui, IGuiPosition parent, GuiRectangle position, GuiIcon overlay) {
            super(gui, parent, position);
            this.overlay = overlay;
        }

        @Override
        public void drawBackground(float partialTicks) {
            GuiUtil.drawFluid(this, tank);
            if (overlay != null) {
                overlay.drawCutInside(this);
            }
        }

        @Override
        public void onMouseClicked(int button) {
            if (contains(gui.mouse)) {
                WidgetFluidTank.this.sendWidgetData(buffer -> buffer.writeByte(NET_CLICK));
            }
        }

        @Override
        public void addToolTips(List<ToolTip> tooltips) {
            if (contains(gui.mouse)) {
                ToolTip tooltip = tank.getToolTip();
                tooltip.refresh();
                tooltips.add(tooltip);
            }
        }

        @Override
        public HelpPosition getHelpInfo() {
            return tank.helpInfo.target(this.expand(4));
        }
    }
}
