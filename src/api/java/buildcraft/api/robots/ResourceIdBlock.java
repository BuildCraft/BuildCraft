/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.robots;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

public class ResourceIdBlock extends ResourceId {

    public ResourceIdBlock() {

    }

    public ResourceIdBlock(int x, int y, int z) {
        pos = new BlockPos(x, y, z);
    }

    public ResourceIdBlock(BlockPos iIndex) {
        pos = iIndex;
    }

    public ResourceIdBlock(TileEntity tile) {
        pos = tile.getPos();
    }

}
