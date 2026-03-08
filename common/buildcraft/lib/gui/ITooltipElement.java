/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;

import java.util.List;

/** Defines some sort of element that should be queried to get tooltips that should be shown. */
@FunctionalInterface
public interface ITooltipElement {
    /** Called to add tooltips to the list of existing tool tips. You MUST refresh the Tooltips if they need to be
     * refreshed.*/
    void addToolTips(List<ToolTip> tooltips);
}
