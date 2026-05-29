/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// STUB(R.Chen): the full Forge BlockUtil (block breaking, BreakEvent firing, fluid drain/fill via the
// Forge fluid API, comparator/double-chest helpers, CompatManager ghost-loading) is deferred. Only the
// block-state / block-entity accessors used by TileBC_Neptune are migrated; the `force` flag — which
// drove CompatManager's chunk-ghost-loading — is now a no-op since Yarn's World loads as needed.
public class BlockUtil {

    public static BlockEntity getTileEntity(World world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static BlockEntity getTileEntity(World world, BlockPos pos, boolean force) {
        return world.getBlockEntity(pos);
    }

    public static BlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(World world, BlockPos pos, boolean force) {
        return world.getBlockState(pos);
    }
}
