/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.block.material.Material;

import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.builders.block.BlockBuilder_Neptune;
import buildcraft.builders.block.BlockFiller_Neptune;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockLibrary_Neptune;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.builders.tile.TileBuilder_Neptune;
import buildcraft.builders.tile.TileFiller_Neptune;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCBuildersBlocks {
    private static final boolean DEV = BCLib.DEVELOPER;

    public static BlockArchitect_Neptune architect;
    public static BlockBuilder_Neptune builder;
    public static BlockFiller_Neptune filler;
    public static BlockLibrary_Neptune library;
    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void preInit() {
        if (DEV) {
            architect = BlockBCBase_Neptune.register(new BlockArchitect_Neptune(Material.IRON, "block.architect"));
            builder = BlockBCBase_Neptune.register(new BlockBuilder_Neptune(Material.IRON, "block.builder"));
            filler = BlockBCBase_Neptune.register(new BlockFiller_Neptune(Material.IRON, "block.filler"));
            library = BlockBCBase_Neptune.register(new BlockLibrary_Neptune(Material.IRON, "block.library"));
        }
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));
        quarry = BlockBCBase_Neptune.register(new BlockQuarry(Material.ROCK, "block.quarry"));

        if (DEV) {
            TileBC_Neptune.registerTile(TileArchitect_Neptune.class, "tile.architect");
            TileBC_Neptune.registerTile(TileBuilder_Neptune.class, "tile.builder");
            TileBC_Neptune.registerTile(TileFiller_Neptune.class, "tile.filler");
            TileBC_Neptune.registerTile(TileLibrary_Neptune.class, "tile.library");
        }
        TileBC_Neptune.registerTile(TileQuarry.class, "tile.quarry");
    }
}
