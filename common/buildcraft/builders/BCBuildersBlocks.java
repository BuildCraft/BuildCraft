/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.block.*;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCBuildersBlocks {
    public static BlockArchitect_Neptune architect;
    public static BlockBuilder_Neptune builder;
    public static BlockLibrary_Neptune library;
    public static BlockFrame frame;
    public static BlockQuarry quarry;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitect_Neptune(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder_Neptune(Material.IRON, "block.builder"));
        library = BlockBCBase_Neptune.register(new BlockLibrary_Neptune(Material.IRON, "block.library"));
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));
        quarry = BlockBCBase_Neptune.register(new BlockQuarry(Material.ROCK, "block.quarry"));

        TileBC_Neptune.registerTile(TileArchitect_Neptune.class, "tile.architect");
        // TODO: builder
        TileBC_Neptune.registerTile(TileLibrary_Neptune.class, "tile.library");
        TileBC_Neptune.registerTile(TileQuarry.class, "tile.quarry");
    }
}
