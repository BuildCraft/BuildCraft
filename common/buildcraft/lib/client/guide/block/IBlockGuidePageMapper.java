/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Deprecated(forRemoval = true)
public interface IBlockGuidePageMapper {
    /** @param world The world object to query
     * @param pos The position to query
     * @param state The state of the block
     * @return What to append to the block location (So, returning "quartz" would give a complete resource location of
     *         "modname:guide/block/quartz.md") */
    String getFor(Level world, BlockPos pos, BlockState state);

    /** @return A complete list of all the possible pages that can be returned by
     *         {@link #getFor(Level, BlockPos, BlockState)} */
    List<String> getAllPossiblePages();
}
