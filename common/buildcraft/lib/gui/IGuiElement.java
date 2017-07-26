/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

/** Defines an element that can be irendered, that exists inside of a rectangle. */
@SideOnly(Side.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(float partialTicks) {}

    default void drawForeground(float partialTicks) {}

    default void tick() {}

    /** {@inheritDoc}
     * <p>
     * This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    default void addToolTips(List<ToolTip> tooltips) {}

    @Override
    default void addHelpElements(List<HelpPosition> elements) {}

    default List<IGuiElement> getThisAndChildrenAt(int x, int y) {
        if (contains(x, y)) {
            return ImmutableList.of(this);
        } else {
            return ImmutableList.of();
        }
    }
}
