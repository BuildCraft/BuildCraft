/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import com.mojang.blaze3d.vertex.PoseStack;

public class GuidePartNewPage extends GuidePart {
    public GuidePartNewPage(GuiGuide gui) {
        super(gui);
    }

    @Override
    public PagePosition renderIntoArea(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        return current.newPage();
    }

    @Override
    public PagePosition handleMouseClick(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        return current.newPage();
    }
}
