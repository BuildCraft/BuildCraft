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
package buildcraft.builders;

import net.minecraft.block.material.Material;

import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.builders.block.BlockBuilder_Neptune;
import buildcraft.builders.block.BlockLibrary_Neptune;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

public class BCBuildersBlocks {
    public static BlockArchitect_Neptune architect;
    public static BlockBuilder_Neptune builder;
    public static BlockLibrary_Neptune library;

    public static void preInit() {
        architect = BlockBCBase_Neptune.register(new BlockArchitect_Neptune(Material.IRON, "block.architect"));
        builder = BlockBCBase_Neptune.register(new BlockBuilder_Neptune(Material.IRON, "block.builder"));
        library = BlockBCBase_Neptune.register(new BlockLibrary_Neptune(Material.IRON, "block.library"));

        TileBC_Neptune.registerTile(TileArchitect_Neptune.class, "tile.architect");
        // TODO: builder
        TileBC_Neptune.registerTile(TileLibrary_Neptune.class, "tile.library");
    }
}
