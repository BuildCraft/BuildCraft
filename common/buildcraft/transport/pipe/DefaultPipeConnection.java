/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.api.transport.pipe.ICustomPipeConnection;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
//        AxisAlignedBB bb = state.getCollisionBoundingBox(world, pos);
        VoxelShape bb = state.getShape(world, pos);
//        if (bb == null)
        if (bb == null || bb.isEmpty()) {
            return 0;
        }

        switch (face) {
            case DOWN:
//                return (float) bb.minY;
                return (float) bb.bounds().minY;
            case UP:
//                return 1 - (float) bb.maxY;
                return 1 - (float) bb.bounds().maxY;
            case NORTH:
//                return (float) bb.minZ;
                return (float) bb.bounds().minZ;
            case SOUTH:
//                return 1 - (float) bb.maxZ;
                return 1 - (float) bb.bounds().maxZ;
            case WEST:
//                return (float) bb.minX;
                return (float) bb.bounds().minX;
            case EAST:
//                return 1 - (float) bb.maxX;
                return 1 - (float) bb.bounds().maxX;
            default:
                return 0;
        }
    }
}
