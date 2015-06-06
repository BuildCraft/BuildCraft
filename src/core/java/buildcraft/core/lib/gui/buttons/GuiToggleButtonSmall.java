/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui.buttons;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToggleButtonSmall extends GuiToggleButton {

    public GuiToggleButtonSmall(int i, int j, int k, String s, boolean active) {
        this(i, j, k, 200, s, active);
    }

    public GuiToggleButtonSmall(int i, int x, int y, int w, String s, boolean active) {
        super(i, x, y, w, StandardButtonTextureSets.SMALL_BUTTON, s, active);
        this.active = active;
    }
}
