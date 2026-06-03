/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide page rendering deferred — GuiGuide icon/layout migration pending
package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.guide.GuiGuide;

@Environment(EnvType.CLIENT)
public abstract class GuidePageBase extends GuidePart {

    protected GuiGuide gui;
    protected int currentPage = 0;
    protected int numPages = 1;

    public GuidePageBase(GuiGuide gui) {
        super(gui);
        this.gui = gui;
    }

    public abstract String getTitle();

    public abstract List<GuideChapter> getChapters();

    public boolean shouldPersistHistory() {
        return true;
    }

    public GuidePageBase createReloaded() {
        return this;
    }

    public final void nextPage() {
        if (currentPage + 1 < numPages) currentPage++;
    }

    public final void lastPage() {
        if (currentPage > 0) currentPage--;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void tick() {}

    public void renderFirstPage(int x, int y, int width, int height) {}

    public void renderSecondPage(int x, int y, int width, int height) {}

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return current;
    }

    // @Override removed (R.Chen): no longer overrides — Phase 10
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
            int mouseX, int mouseY, int mouseButton) {
        return null;
    }
}
