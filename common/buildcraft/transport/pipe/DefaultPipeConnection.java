/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
        // TODO(R.Chen): getCollisionBoundingBox → getCollisionShape(world,pos).getBoundingBox()
        VoxelShape shape = state.getCollisionShape(world, pos);
        if (shape.isEmpty()) {
            return 0;
        }
        Box bb = shape.getBoundingBox();

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
