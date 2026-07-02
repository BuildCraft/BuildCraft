/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.elem;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiElementSimple;
import ct.buildcraft.lib.gui.ISimpleDrawable;
import ct.buildcraft.lib.gui.pos.IGuiArea;

public class GuiElementDrawable extends GuiElementSimple {
    private final ISimpleDrawable drawable;
    private final INodeBoolean visible;
    private final boolean foreground;

    public GuiElementDrawable(BuildCraftGui gui, IGuiArea element, ISimpleDrawable drawable, boolean foreground) {
        this(gui, element, drawable, foreground, NodeConstantBoolean.TRUE);
    }

    public GuiElementDrawable(BuildCraftGui gui, IGuiArea element, ISimpleDrawable drawable, boolean foreground, INodeBoolean visible) {
        super(gui, element);
        this.drawable = drawable;
        this.visible = visible;
        this.foreground = foreground;
    }

    @Override
    public void drawBackground(PoseStack pose, float partialTicks) {
        if (!foreground) {
            draw(pose);
        }
    }

    @Override
    public void drawForeground(PoseStack pose, float partialTicks) {
        if (foreground) {
            draw(pose);
        }
    }

    private void draw(PoseStack pose) {
        if (visible.evaluate()) {
            drawable.drawAt(pose, this);
        }
    }
}
