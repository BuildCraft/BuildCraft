/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */

package buildcraft.lib.gui.button;

import buildcraft.lib.gui.IGuiElement;

public interface IButtonClickEventListener {
    /** @param button
     * @param buttonKey The key of the button. Will always be 0 if called from minecraft code (as minecraft doesn't
     *            support button clicks with any other numbers), as opposed to if this was called from
     *            {@link IGuiElement} code, in which case all button keys will be listened to. */
    void handleButtonClick(IButtonClickEventTrigger button, int buttonKey);
}
