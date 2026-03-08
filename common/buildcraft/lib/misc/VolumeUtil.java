/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Provides methods for iterating over a specific volume in a world. */
public class VolumeUtil {
    public static void iterateCone(World world, BlockPos start, Direction direction, int distance, boolean edges, VolumeIterator iter) {
        Cone cone = edges ? Cone.SQUARE : Cone.SQUARE;
        iterateVolume(world, start, direction, distance, cone, iter);
    }

    public static void iterateVolume(World world, BlockPos start, Direction direction, int distance, VolumeProducer producer, VolumeIterator iter) {
        List<VisiblePos> volume = producer.getVolume(world, start, direction, distance);
        Set<BlockPos> allVisible = new HashSet<>();
        allVisible.add(start);
        for (VisiblePos possible : volume) {
            Set<BlockPos> unknown = new HashSet<>(possible.required);
            unknown.removeAll(allVisible);
            boolean visible = true;
            for (BlockPos p : unknown) {
                if (world.isEmptyBlock(p)) {
                    allVisible.add(p);
                } else {
                    visible = false;
                    break;
                }
            }
            iter.takePos(world, start, possible.pos, visible);
        }
    }

    public static class VisiblePos {
        public final BlockPos pos;
        public final List<BlockPos> required;

        public VisiblePos(BlockPos pos, BlockPos to) {
            this.pos = pos;
            this.required = PositionUtil.getAllOnPath(pos, to);
        }
    }

    public interface VolumeProducer {
        List<VisiblePos> getVolume(World world, BlockPos start, Direction direction, int distance);
    }

    public enum Cone implements VolumeProducer {
        SQUARE(true),
        PYRAMID(false);

        private final boolean edges;

        Cone(boolean edges) {
            this.edges = edges;
        }

        @Override
        public List<VisiblePos> getVolume(World world, BlockPos start, Direction direction, int distance) {
            List<VisiblePos> list = new ArrayList<>();
            final Axis axisI, axisJ;
            switch (direction.getAxis()) {
                case X:
                    axisI = Axis.Y;
                    axisJ = Axis.Z;
                    break;
                case Y:
                    axisI = Axis.X;
                    axisJ = Axis.Z;
                    break;
                case Z:
                default:
                    axisI = Axis.X;
                    axisJ = Axis.Y;
                    break;
            }
            Direction faceI = Direction.get(AxisDirection.POSITIVE, axisI);
            Direction faceJ = Direction.get(AxisDirection.POSITIVE, axisJ);

            BlockPos coneCenter = start;
            for (int d = 0; d < distance; d++) {
                // Firstly expand the visGraph
                coneCenter = coneCenter.relative(direction);
                for (int i = -d; i <= d; i++) {
                    for (int j = -d; j <= d; j++) {
                        if (!edges) {
                            int dist = Math.abs(i) + Math.abs(j);
                            if (dist > d) {
                                continue;
                            }
                        }
                        BlockPos posAt = coneCenter.relative(faceI, i).relative(faceJ, j);
                        list.add(new VisiblePos(posAt, start));
                    }
                }
            }

            return list;
        }
    }

    public interface VolumeIterator {
        void takePos(World world, BlockPos start, BlockPos pos, boolean visible);
    }
}
