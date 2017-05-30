/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.block.material.Material;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockElectronicLibrary;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;

public class BCBuildersBlocks {
    public static BlockArchitectTable architect;
    public static BlockBuilder builder;
    public static BlockFiller filler;
    public static BlockElectronicLibrary library;

    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitectTable(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder(Material.IRON, "block.builder"));
        library = BlockBCBase_Neptune.register(new BlockElectronicLibrary(Material.IRON, "block.library"));
        filler = BlockBCBase_Neptune.register(new BlockFiller(Material.IRON, "block.filler"));
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));
        quarry = BlockBCBase_Neptune.register(new BlockQuarry(Material.ROCK, "block.quarry"));

        TileBC_Neptune.registerTile(TileArchitectTable.class, "tile.architect");
        TileBC_Neptune.registerTile(TileBuilder.class, "tile.builder");
        TileBC_Neptune.registerTile(TileFiller.class, "tile.filler");
        TileBC_Neptune.registerTile(TileElectronicLibrary.class, "tile.library");
        TileBC_Neptune.registerTile(TileQuarry.class, "tile.quarry");
    }
}
