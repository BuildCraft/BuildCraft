/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public class GuideChapterWithin extends GuideChapter {
    private int lastPage = -1;

    // public GuideChapterWithin(GuiGuide gui, int level, String text)
    public GuideChapterWithin(GuiGuide gui, int level, String textKey, Component text) {
//        super(gui, level, text);
        super(gui, level, textKey, text);
    }

    // public GuideChapterWithin(GuiGuide gui, String chapter)
    public GuideChapterWithin(GuiGuide gui, String chapterKey, Component chapter) {
//        this(gui, 0, chapter);
        this(gui, 0, chapterKey, chapter);
    }

    @Override
    public PagePosition renderIntoArea(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        PagePosition pos = super.renderIntoArea(poseStack, x, y, width, height, current, index);
        lastPage = pos.page;
        if (pos.pixel == 0) {
            lastPage = pos.page - 1;
        }
        return pos;
    }

    @Override
    protected boolean onClick() {
        if (lastPage != -1) {
            GuidePageBase page = gui.getCurrentPage();
            if (page.getChapters().contains(this)) {
                page.goToPage(lastPage);
                return true;
            }
        }
        return false;
    }
}
