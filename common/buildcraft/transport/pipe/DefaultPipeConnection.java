/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        AxisAlignedBB bb = state.getCollisionBoundingBox(world, pos);
        if (bb == null) {
            return 0;
        }

        switch (face) {
            case DOWN:
                return (float) bb.minY;
            case UP:
                return 1 - (float) bb.maxY;
            case NORTH:
                return (float) bb.minZ;
            case SOUTH:
                return 1 - (float) bb.maxZ;
            case WEST:
                return (float) bb.minX;
            case EAST:
                return 1 - (float) bb.maxX;
            default:
                return 0;
        }
    }
}
