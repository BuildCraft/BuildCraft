/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiPosition;

// STUB(R.Chen): GUI render — Phase 5.
@Environment(EnvType.CLIENT)
public interface IContainingElement extends IInteractionElement {
    List<IGuiElement> getChildElements();
    default IGuiPosition getChildElementPosition() { return this; }
    default void calculateSizes() {}
    @Override default void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement e : getChildElements()) e.addToolTips(tooltips);
    }
    @Override default void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement e : getChildElements()) e.addHelpElements(elements);
    }
}
