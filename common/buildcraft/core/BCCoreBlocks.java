/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.util.ArrayList;

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
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;

import buildcraft.core.block.BlockDecoration;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.block.BlockSpring;
import buildcraft.core.item.ItemBlockDecorated;
import buildcraft.core.item.ItemBlockSpring;
import buildcraft.core.item.ItemEngine_BC8;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;

@Mod.EventBusSubscriber(modid = BCCore.MODID)
@GameRegistry.ObjectHolder(BCCore.MODID)
public class BCCoreBlocks {
    public static final BlockEngine_BC8 engine = null;
    public static final BlockSpring spring = null;
    public static final BlockDecoration decorated = null;
    @GameRegistry.ObjectHolder("marker_volume")
    public static final BlockMarkerVolume markerVolume = null;
    @GameRegistry.ObjectHolder("marker_path")
    public static final BlockMarkerPath markerPath = null;

    private static ArrayList<IItemBuildCraft> items = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        BlockEngine_BC8 tempEngine = new BlockEngine_BC8(Material.IRON, "block.engine.bc");
        tempEngine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        tempEngine.registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new);
        event.getRegistry().registerAll(
            new BlockSpring("block.spring"),
            new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"),
            new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"),
            tempEngine
        );

        if (BCLib.DEV) {
            event.getRegistry().register(new BlockDecoration("block.decorated"));
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        items.clear();
        ItemBlockBC_Neptune[] blocks = {
            new ItemBlockSpring(spring),
            new ItemEngine_BC8<>(engine),
            new ItemBlockBC_Neptune(markerPath),
            new ItemBlockBC_Neptune(markerVolume)
        };
        for (ItemBlockBC_Neptune itemblock: blocks) {
            items.add(itemblock);
            event.getRegistry().register(itemblock);
        }

        if (BCLib.DEV) {
            ItemBlockBC_Neptune itemblock = new ItemBlockDecorated(decorated);
            items.add(itemblock);
            event.getRegistry().register(itemblock);
        }
    }

    @SubscribeEvent
    public static void registerVariants(ModelRegistryEvent event) {
        items.forEach(IItemBuildCraft::registerVariants);
    }

}
