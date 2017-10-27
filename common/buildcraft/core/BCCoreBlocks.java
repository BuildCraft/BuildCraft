/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.enums.EnumEngineType;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.tile.TileBC_Neptune;

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

@Mod.EventBusSubscriber(modid = BCCore.MODID)
@GameRegistry.ObjectHolder(BCCore.MODID)
public class BCCoreBlocks {
    public static final BlockEngine_BC8 ENGINE = null;
    public static final BlockSpring SPRING = null;
    public static final BlockDecoration DECORATED = null;
    public static final BlockMarkerVolume MARKER_VOLUME = null;
    public static final BlockMarkerPath MARKER_PATH = null;
    public static final BlockPowerConsumerTester powerTester = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        BlockEngine_BC8 tempEngine = new BlockEngine_BC8(Material.IRON, "block.engine.bc");
        tempEngine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        tempEngine.registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new);
        RegistryHelper.registerBlocks(event,
            new BlockSpring("block.spring"),
            new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"),
            new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"),
            tempEngine
        );

        if (BCLib.DEV) {
            RegistryHelper.registerBlocks(event, new BlockDecoration("block.decorated"),
                new BlockPowerConsumerTester(Material.IRON, "block.power_tester"));
        }

        TileBC_Neptune.registerTile(TileMarkerVolume.class, "tile.marker.volume");
        TileBC_Neptune.registerTile(TileMarkerPath.class, "tile.marker.path");
        TileBC_Neptune.registerTile(TileEngineRedstone_BC8.class, "tile.engine.wood");
        TileBC_Neptune.registerTile(TileEngineCreative.class, "tile.engine.creative");
        TileBC_Neptune.registerTile(TilePowerConsumerTester.class, "tile.power_tester");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        RegistryHelper.registerItems(event,
            new ItemBlockSpring(SPRING),
            new ItemEngine_BC8<>(ENGINE),
            new ItemBlockBC_Neptune(MARKER_PATH),
            new ItemBlockBC_Neptune(MARKER_VOLUME)
        );

        if (BCLib.DEV) {
            event.getRegistry().register(new ItemBlockDecorated(DECORATED));
        }
    }

    @SubscribeEvent
    public static void registerVariants(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            ENGINE,
            SPRING,
            DECORATED,
            MARKER_VOLUME,
            MARKER_PATH
        );
    }

}
