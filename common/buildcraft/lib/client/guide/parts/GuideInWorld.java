/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.world.WorldState;
import net.minecraft.client.gui.GuiGraphics;

public class GuideInWorld extends GuidePart {
    private boolean fullscreen = false;
    private final WorldState state;

    public GuideInWorld(GuiGuide gui, boolean fullscreen, WorldState state) {
        super(gui);
        this.fullscreen = fullscreen;
        this.state = state;
    }

    @Override
    public PagePosition renderIntoArea(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public PagePosition handleMouseClick(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }
}
