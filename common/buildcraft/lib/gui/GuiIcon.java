/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.pos.IGuiArea;

// STUB(R.Chen): GL11/BufferBuilder/Tessellator render — Phase 5.
@Environment(EnvType.CLIENT)
public class GuiIcon implements ISimpleDrawable {
    public GuiIcon(int x, int y, int u, int v, int w, int h) {}
    public GuiIcon(GuiIcon parent, int relU, int relV) {}
    @Override public void drawAt(double x, double y) {}
    public void drawCutInside(IGuiArea element) {}
    public void drawScaled(double x, double y, double w, double h) {}
}
