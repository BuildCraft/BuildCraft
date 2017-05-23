/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.world.WorldInfo;

public class GuideInWorldFactory implements GuidePartFactory {
    private final WorldInfo info;

    public GuideInWorldFactory(WorldInfo info) {
        this.info = info;
    }

    @Override
    public GuideInWorld createNew(GuiGuide gui) {
        return null;
    }
}
