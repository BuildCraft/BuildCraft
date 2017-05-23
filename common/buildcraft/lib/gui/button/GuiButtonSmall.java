/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.gui.button;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;

@SideOnly(Side.CLIENT)
public class GuiButtonSmall extends GuiBetterButton {

    public GuiButtonSmall(GuiBC8<?> gui, int i, int x, int y, String s) {
        this(gui, i, x, y, 200, s);
    }

    public GuiButtonSmall(GuiBC8<?> gui, int i, int x, int y, int w, String s) {
        super(gui, i, x, y, w, StandardButtonTextureSets.SMALL_BUTTON, s);
    }
}
