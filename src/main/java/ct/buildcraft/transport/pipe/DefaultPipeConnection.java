/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe;

import ct.buildcraft.api.transport.pipe.ICustomPipeConnection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
        VoxelShape shape = state.getCollisionShape(world, pos);
        if (shape.isEmpty()) {
            return 0;
        }
        AABB bb = shape.bounds();
        switch (face) {
            case DOWN:
                return (float) bb.minY;
            case UP:
                return 1f - (float) bb.maxY;
            case NORTH:
                return (float) bb.minZ;
            case SOUTH:
                return 1f - (float) bb.maxZ;
            case WEST:
                return (float) bb.minX;
            case EAST:
                return 1f - (float) bb.maxX;
            default:
                return 0f;
        }
    }
}
