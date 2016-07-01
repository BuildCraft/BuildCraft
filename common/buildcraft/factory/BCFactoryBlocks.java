/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.factory;

import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPlainPipe;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCFactoryBlocks {
    public static BlockAutoWorkbenchItems autoWorkbenchItems;
    public static BlockMiningWell miningWell;
    public static BlockPlainPipe plainPipe;
    // public static BlockAutoWorkbenchFluids autoWorkbenchFluids;
    // public static BlockPlastic plastic;

    public static void preInit() {
        // plastic = BlockBuildCraftBase_BC8.register(new BlockPlastic("block.plastic"), ItemPlastic::new);
        autoWorkbenchItems = BlockBCBase_Neptune.register(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));
        miningWell = BlockBCBase_Neptune.register(new BlockMiningWell(Material.ROCK, "block.mining_well"));
        plainPipe = BlockBCBase_Neptune.register(new BlockPlainPipe(Material.ROCK, "block.plain_pipe"));

        TileBC_Neptune.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
        TileBC_Neptune.registerTile(buildcraft.factory.tile.TileMiningWell.class, "tile.mining_well");
    }
}
