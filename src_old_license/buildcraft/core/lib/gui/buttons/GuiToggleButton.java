/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui.buttons;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.GuiBetterButton;
import buildcraft.lib.gui.button.IButtonTextureSet;
import buildcraft.lib.gui.button.StandardButtonTextureSets;

/**
 * Made useless by IButtonBehaviour
 */
public class GuiToggleButton extends GuiBetterButton {

    public GuiToggleButton(GuiBC8<?> gui, int id, int x, int y, String label, boolean active) {
        this(gui, id, x, y, 200, StandardButtonTextureSets.LARGE_BUTTON, label, active);
    }

    public GuiToggleButton(GuiBC8<?> gui, int id, int x, int y, int width, String s, boolean active) {
        super(gui, id, x, y, width, StandardButtonTextureSets.LARGE_BUTTON, s);
        this.active = active;
    }

    public GuiToggleButton(GuiBC8<?> gui, int id, int x, int y, int width, IButtonTextureSet texture, String s, boolean active) {
        super(gui, id, x, y, width, texture, s);
        this.active = active;
    }

    public void toggle() {
        active = !active;
    }
}
