/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import buildcraft.api.enums.EnumEngineType;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCEnergyBlocks {
    public static void preInit() {
        if (BCCoreBlocks.engine != null) {
            TileBC_Neptune.registerTile(TileEngineStone_BC8.class, "tile.engine.stone");
            BCCoreBlocks.engine.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new);
        }
    }
}
