/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GuidePage rendering deferred
package buildcraft.lib.client.guide.parts;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.guide.GuiGuide;

@Environment(EnvType.CLIENT)
public class GuidePage extends GuidePageBase {

    public GuidePage(GuiGuide gui) {
        super(gui);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public List<GuideChapter> getChapters() {
        return Collections.emptyList();
    }
}
