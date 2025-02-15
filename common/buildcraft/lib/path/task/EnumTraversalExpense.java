/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path.task;

import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public enum EnumTraversalExpense {
    AIR((byte) 1),
    FLUID((byte) 3),
    /** If you *must* find a path then you can use this, but this isn't included in any of the graphs. */
    SOLID((byte) -1);

    public final byte expense;

    EnumTraversalExpense(byte expense) {
        this.expense = expense;
    }

    public static EnumTraversalExpense getFor(Level world, BlockPos pos) {
        return getFor(world, pos, world.getBlockState(pos));
    }

    public static EnumTraversalExpense getFor(Level world, BlockPos pos, BlockState state) {
        if (world.isEmptyBlock(pos)) {
            return AIR;
        }
//        Material mat = state.getMaterial();
//        if (mat.isLiquid())
        if (BlockUtil.isFluidBlock(state)) {
            return FLUID;
        }
//        Block block = state.getBlock();
//        if (block.isPassable(world, pos))
        if (state.isPathfindable(world, pos, PathComputationType.LAND)) {
            return AIR;
        }
        return SOLID;
    }
}
