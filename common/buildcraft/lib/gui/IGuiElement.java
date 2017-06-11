/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

/** Defines an element that can be interacted with, that exists inside of a rectangle. */
@SideOnly(Side.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement {
    default void drawBackground(float partialTicks) {}

    default void drawForeground(float partialTicks) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}

    @Override
    default void addToolTips(List<ToolTip> tooltips) {}

    /** @return The {@link HelpPosition} pair, or null if this element shouldn't display help right now. */
    default HelpPosition getHelpInfo() {
        return null;
    }
}
