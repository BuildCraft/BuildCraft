/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import buildcraft.api.net.IMessage;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.net.PacketBufferBC;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.List;

public class WidgetFluidTank<C extends ContainerBC_Neptune<?>> extends Widget_Neptune<C> {
    private static final byte NET_CLICK = 0;

    private final Tank tank;

    public WidgetFluidTank(C container, Tank tank) {
        super(container);
        this.tank = tank;
    }

    @Override
    public IMessage handleWidgetDataServer(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            tank.onGuiClicked(container);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public IGuiElement createGuiElement(BuildCraftGui gui, IGuiArea area, GuiIcon overlay) {
        return new GuiElementFluidTank(gui, area, overlay);
    }

    private final class GuiElementFluidTank extends GuiElementSimple implements IInteractionElement {
        private final GuiIcon overlay;

        public GuiElementFluidTank(BuildCraftGui gui, IGuiArea area, GuiIcon overlay) {
            super(gui, area);
            this.overlay = overlay;
        }

        @Override
        public void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
            GuiUtil.drawFluid(this, tank, guiGraphics);
            if (overlay != null) {
                overlay.drawCutInside(this, guiGraphics);
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
        public void addHelpElements(List<HelpPosition> elements) {
            elements.add(tank.helpInfo.target(this.expand(4)));
        }
    }
}
