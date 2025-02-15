/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/** Defines an element that can be irendered, that exists inside of a rectangle. */
@OnlyIn(Dist.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
    }

    default void drawForeground(GuiGraphics guiGraphics, float partialTicks) {
    }

    default void tick() {
    }

    /** {@inheritDoc}
     * <p>
     * This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    default void addToolTips(List<ToolTip> tooltips) {
    }

    @Override
    default void addHelpElements(List<HelpPosition> elements) {
    }

    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        if (contains(x, y)) {
            return ImmutableList.of(this);
        } else {
            return ImmutableList.of();
        }
    }

    /** Add debugging information to the list. Note that a lot of elements will be called for this, so keep the amount
     * of information minimal.
     *
     * @return An identifier for this element (usually a name) */
    default String getDebugInfo(List<String> info) {
        return toString();
    }
}
