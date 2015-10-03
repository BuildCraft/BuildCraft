/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.builders.tile.TilePathMarker;

public class BlockPathMarker extends BlockMarker {

    public BlockPathMarker() {}

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TilePathMarker();
    }
}
