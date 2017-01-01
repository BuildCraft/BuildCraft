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
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarkerVolume markerVolume;
    public static BlockMarkerPath markerPath;

    public static void preInit() {
        spring = BlockBCBase_Neptune.register(new BlockSpring("block.spring"), ItemBlockSpring::new);
        markerVolume = BlockBCBase_Neptune.register(new BlockMarkerVolume(Material.CIRCUITS, "block.marker.volume"));
        markerPath = BlockBCBase_Neptune.register(new BlockMarkerPath(Material.CIRCUITS, "block.marker.path"));
        decorated = BlockBCBase_Neptune.register(new BlockDecoration("block.decorated"), ItemBlockDecorated::new);

        engine = BlockBCBase_Neptune.register(new BlockEngine_BC8(Material.IRON, "block.engine.bc"), ItemEngine_BC8::new);
        engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
        engine.registerEngine(EnumEngineType.CREATIVE, TileEngineCreative::new);

        TileBC_Neptune.registerTile(TileMarkerVolume.class, "tile.marker.volume");
        TileBC_Neptune.registerTile(TileMarkerPath.class, "tile.marker.path");
        TileBC_Neptune.registerTile(TileEngineRedstone_BC8.class, "tile.engine.wood");
        TileBC_Neptune.registerTile(TileEngineCreative.class, "tile.engine.creative");
    }
}
