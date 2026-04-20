/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.core;

import net.minecraft.core.BlockPos;

/** A cuboid volume. BuildCraft's default implementation is mutable, so you should not cache instances that you do not
 * own as-is, without making an immutable copy first. */
public interface IBox extends IZone {
    IBox expand(int amount);

    IBox contract(int amount);

    BlockPos min();

    BlockPos max();

    default BlockPos size() {
        return max().subtract(min()).offset(1, 1, 1);
    }
}
