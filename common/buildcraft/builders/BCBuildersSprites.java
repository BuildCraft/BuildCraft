/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCBuildersSprites {
    public static final SpriteHolder FILLING_PLANNER;
    public static final SpriteHolder ROBOT;

    static {
        FILLING_PLANNER = getHolder("addons/filling_planner");
        ROBOT = getHolder("robot");
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftbuilders:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
