/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.block.material.Material;

import buildcraft.builders.block.BlockArchitect;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockLibrary;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileLibrary;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCBuildersBlocks {
    public static BlockArchitect architect;
    public static BlockBuilder builder;
    public static BlockFiller filler;
    public static BlockLibrary library;
    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitect(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder(Material.IRON, "block.builder"));
        filler = BlockBCBase_Neptune.register(new BlockFiller(Material.IRON, "block.filler"));
        library = BlockBCBase_Neptune.register(new BlockLibrary(Material.IRON, "block.library"));
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));
        quarry = BlockBCBase_Neptune.register(new BlockQuarry(Material.ROCK, "block.quarry"));

        TileBC_Neptune.registerTile(TileArchitect.class, "tile.architect");
        TileBC_Neptune.registerTile(TileBuilder.class, "tile.builder");
        TileBC_Neptune.registerTile(TileFiller.class, "tile.filler");
        TileBC_Neptune.registerTile(TileLibrary.class, "tile.library");
        TileBC_Neptune.registerTile(TileQuarry.class, "tile.quarry");
    }
}
