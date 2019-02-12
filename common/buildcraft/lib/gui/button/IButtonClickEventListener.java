/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import buildcraft.lib.gui.IGuiElement;

public interface IButtonClickEventListener {
    /** @param buttonKey The key of the button. Will always be 0 if called from minecraft code (as minecraft doesn't
     *            support button clicks with any other numbers), as opposed to if this was called from
     *            {@link IGuiElement} code, in which case all button keys will be listened to. */
    void handleButtonClick(IButtonClickEventTrigger button, int buttonKey);
}
