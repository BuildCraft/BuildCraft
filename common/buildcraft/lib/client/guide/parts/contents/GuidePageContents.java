/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide contents page rendering deferred — GuiTextField compat pending
package buildcraft.lib.client.guide.parts.contents;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;

@Environment(EnvType.CLIENT)
public class GuidePageContents extends GuidePageBase {

    public GuidePageContents(GuiGuide gui) {
        super(gui);
    }

    @Override
    public String getTitle() {
        return "Contents";
    }

    @Override
    public List<GuideChapter> getChapters() {
        return Collections.emptyList();
    }
}
