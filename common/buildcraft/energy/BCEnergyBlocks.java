/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.energy;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;

import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.energy.tile.TileSpringOil;

public class BCEnergyBlocks {
    public static void preInit() {
        EnumSpring.OIL.liquidBlock = BCEnergyFluids.crudeOil[0].getBlock().getDefaultState();
        EnumSpring.OIL.tileConstructor = TileSpringOil::new;
        TileBC_Neptune.registerTile(TileSpringOil.class, "tile.spring.oil");

        if (BCCoreBlocks.engine != null) {
            TileBC_Neptune.registerTile(TileEngineStone_BC8.class, "tile.engine.stone");
            BCCoreBlocks.engine.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new);

            TileBC_Neptune.registerTile(TileEngineIron_BC8.class, "tile.engine.iron");
            BCCoreBlocks.engine.registerEngine(EnumEngineType.IRON, TileEngineIron_BC8::new);
        }
    }
}
