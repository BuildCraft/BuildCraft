/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.util.BlockPos;

import buildcraft.api.core.IAreaProvider;

public class DefaultAreaProvider implements IAreaProvider {

    // Should this just have an internal immutable box?
    BlockPos min, max;

    @Deprecated
    public DefaultAreaProvider(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        this(new BlockPos(xMin, yMin, zMin), new BlockPos(xMax, yMax, zMax));
    }

    public DefaultAreaProvider(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public void removeFromWorld() {}
}
