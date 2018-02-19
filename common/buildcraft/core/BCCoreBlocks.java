/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.core.block.*;
import buildcraft.core.item.ItemBlockDecorated;
import buildcraft.core.item.ItemBlockSpring;
import buildcraft.core.item.ItemEngine_BC8;
import buildcraft.core.tile.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.block.material.Material;

public class BCCoreBlocks {

    public static BlockEngine_BC8 engine;

    public static void preInit() {
        RegistrationHelper.addBlockAndItem(new BlockSpring("block.spring"), ItemBlockSpring::new);
        RegistrationHelper.addBlockAndItem(new BlockDecoration("block.decorated"), ItemBlockDecorated::new);
        RegistrationHelper.addBlockAndItem(new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"));
        RegistrationHelper.addBlockAndItem(new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"));
        if (BCLib.DEV) {
            RegistrationHelper.addBlockAndItem(new BlockPowerConsumerTester(Material.IRON, "block.power_tester"));
        }

        engine = RegistrationHelper.addBlockAndItem(new BlockEngine_BC8(Material.IRON, "block.engine.bc"), ItemEngine_BC8::new);
        if (engine != null) {
            engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
            engine.registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new);
        }

        RegistrationHelper.registerTile(TileMarkerVolume.class, "tile.marker.volume");
        RegistrationHelper.registerTile(TileMarkerPath.class, "tile.marker.path");
        RegistrationHelper.registerTile(TileEngineRedstone_BC8.class, "tile.engine.wood");
        RegistrationHelper.registerTile(TileEngineCreative.class, "tile.engine.creative");
        if (BCLib.DEV) {
            RegistrationHelper.registerTile(TilePowerConsumerTester.class, "tile.power_tester");
        }
    }
}
