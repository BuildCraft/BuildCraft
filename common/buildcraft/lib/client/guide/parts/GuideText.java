/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;

public class GuideText extends GuidePart {
    public final PageLine text;

    public GuideText(GuiGuide gui, String text) {
        this(gui, new PageLine(0, text, false));
    }

    public GuideText(GuiGuide gui, PageLine text) {
        super(gui);
        this.text = text;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return renderLine(current, text, x, y, width, height, index);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        return renderLine(current, text, x, y, width, height, -1);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
