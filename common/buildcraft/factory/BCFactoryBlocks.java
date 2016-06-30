/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.block.material.Material;

import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCFactoryBlocks {
    public static BlockAutoWorkbenchItems autoWorkbenchItems;
    // public static BlockAutoWorkbenchFluids autoWorkbenchFluids;
    // public static BlockPlastic plastic;

    public static void preInit() {
        // plastic = BlockBuildCraftBase_BC8.register(new BlockPlastic("block.plastic"), ItemPlastic::new);
        autoWorkbenchItems = BlockBCBase_Neptune.register(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));

        TileBC_Neptune.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
    }
}
