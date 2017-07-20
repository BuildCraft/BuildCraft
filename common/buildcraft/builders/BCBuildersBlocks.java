/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.block.*;
import buildcraft.builders.tile.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCBuildersBlocks {
    public static BlockArchitectTable architect;
    public static BlockBuilder builder;
    public static BlockFiller filler;
    public static BlockElectronicLibrary library;
    public static BlockReplacer replacer;

    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitectTable(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder(Material.IRON, "block.builder"));
        filler = BlockBCBase_Neptune.register(new BlockFiller(Material.IRON, "block.filler"));
        library = BlockBCBase_Neptune.register(new BlockElectronicLibrary(Material.IRON, "block.library"));
        if (BCLib.DEV) {
            replacer = BlockBCBase_Neptune.register(new BlockReplacer(Material.IRON, "block.replacer"));
        }
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));
        quarry = BlockBCBase_Neptune.register(new BlockQuarry(Material.ROCK, "block.quarry"));

        TileBC_Neptune.registerTile(TileArchitectTable.class, "tile.architect");
        TileBC_Neptune.registerTile(TileBuilder.class, "tile.builder");
        TileBC_Neptune.registerTile(TileFiller.class, "tile.filler");
        TileBC_Neptune.registerTile(TileElectronicLibrary.class, "tile.library");
        if (BCLib.DEV) {
            TileBC_Neptune.registerTile(TileReplacer.class, "tile.replacer");
        }
        TileBC_Neptune.registerTile(TileQuarry.class, "tile.quarry");
    }
}
