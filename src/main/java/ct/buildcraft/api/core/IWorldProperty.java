/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IWorldProperty {
    boolean get(Level world, BlockPos pos);

    void clear();
}
