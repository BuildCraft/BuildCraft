/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.builders.block.BlockBuilder_Neptune;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockLibrary_Neptune;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCBuildersBlocks {
    public static BlockArchitect_Neptune architect;
    public static BlockBuilder_Neptune builder;
    public static BlockLibrary_Neptune library;
    public static BlockFrame frame;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitect_Neptune(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder_Neptune(Material.IRON, "block.builder"));
        library = BlockBCBase_Neptune.register(new BlockLibrary_Neptune(Material.IRON, "block.library"));
        frame = BlockBCBase_Neptune.register(new BlockFrame(Material.ROCK, "block.frame"));

        TileBC_Neptune.registerTile(TileArchitect_Neptune.class, "tile.architect");
        // TODO: builder
        TileBC_Neptune.registerTile(TileLibrary_Neptune.class, "tile.library");
    }
}
