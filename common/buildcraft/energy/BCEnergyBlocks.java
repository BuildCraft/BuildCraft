/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.energy.tile.TileSpringOil;

public class BCEnergyBlocks {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static void preInit() {

        if (BCCoreBlocks.engine != null) {
            BCCoreBlocks.engine.registerEngine(EnumEngineType.STONE, TileEngineStone_BC8::new);
            BCCoreBlocks.engine.registerEngine(EnumEngineType.IRON, TileEngineIron_BC8::new);
        }

        EnumSpring.OIL.liquidBlock = BCEnergyFluids.crudeOil[0].getBlock().getDefaultState();
        EnumSpring.OIL.tileConstructor = TileSpringOil::new;

        HELPER.registerTile(TileSpringOil.class, "tile.spring.oil");
        HELPER.registerTile(TileEngineStone_BC8.class, "tile.engine.stone");
        HELPER.registerTile(TileEngineIron_BC8.class, "tile.engine.iron");
    }
}
