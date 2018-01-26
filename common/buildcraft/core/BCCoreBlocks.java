/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.block.material.Material;

import buildcraft.api.enums.EnumEngineType;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.core.block.BlockDecoration;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.block.BlockPowerConsumerTester;
import buildcraft.core.block.BlockSpring;
import buildcraft.core.item.ItemBlockDecorated;
import buildcraft.core.item.ItemBlockSpring;
import buildcraft.core.item.ItemEngine_BC8;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.core.tile.TilePowerConsumerTester;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarkerVolume markerVolume;
    public static BlockMarkerPath markerPath;
    public static BlockPowerConsumerTester powerTester;

    public static void preInit() {
        spring = RegistrationHelper.addBlockAndItem(new BlockSpring("block.spring"), ItemBlockSpring::new);
        decorated = RegistrationHelper.addBlockAndItem(new BlockDecoration("block.decorated"), ItemBlockDecorated::new);
        markerVolume = RegistrationHelper.addBlockAndItem(new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"));
        markerPath = RegistrationHelper.addBlockAndItem(new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"));
        if (BCLib.DEV) {
            powerTester = RegistrationHelper.addBlockAndItem(new BlockPowerConsumerTester(Material.IRON, "block.power_tester"));
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
