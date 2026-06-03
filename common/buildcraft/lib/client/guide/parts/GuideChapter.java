/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide chapter rendering deferred — GuiGuide constant migration pending
package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.guide.GuiGuide;

@Environment(EnvType.CLIENT)
public abstract class GuideChapter extends GuidePart {

    public final String chapter;
    protected List<GuideChapter> children = new ArrayList<>();

    public GuideChapter(GuiGuide gui, String chapter) {
        super(gui);
        this.chapter = chapter;
    }

    public List<GuideChapter> getChildren() {
        return children;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
            int mouseX, int mouseY, int mouseButton) {
        return null;
    }
}
