/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.core.marker.volume;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

// Yarn 1.20.1 renames:
//   PlayerEntity → PlayerEntity, Direction → Direction, Box → net.minecraft.util.math.Box,
//   HitResult#calculateIntercept → Box#raycast (returns Optional<Vec3d>)
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public enum EnumAddonSlot {
    EAST_UP_SOUTH(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE),
    EAST_UP_NORTH(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE),
    EAST_DOWN_SOUTH(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE),
    EAST_DOWN_NORTH(Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE),
    WEST_UP_SOUTH(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE),
    WEST_UP_NORTH(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE),
    WEST_DOWN_SOUTH(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE),
    WEST_DOWN_NORTH(Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE);

    public static final EnumAddonSlot[] VALUES = values();

    public final Map<Direction.Axis, Direction.AxisDirection> directions = new EnumMap<>(Direction.Axis.class);

    EnumAddonSlot(Direction.AxisDirection x, Direction.AxisDirection y, Direction.AxisDirection z) {
        directions.put(Direction.Axis.X, x);
        directions.put(Direction.Axis.Y, y);
        directions.put(Direction.Axis.Z, z);
    }

    public Box getBoundingBox(VolumeBox volumeBox) {
        Box aabb = volumeBox.box.getBoundingBox();
        Vec3d boxOffset = new Vec3d(
            directions.get(Direction.Axis.X) == Direction.AxisDirection.POSITIVE ? aabb.maxX : aabb.minX,
            directions.get(Direction.Axis.Y) == Direction.AxisDirection.POSITIVE ? aabb.maxY : aabb.minY,
            directions.get(Direction.Axis.Z) == Direction.AxisDirection.POSITIVE ? aabb.maxZ : aabb.minZ
        );
        return new Box(
            boxOffset.x,
            boxOffset.y,
            boxOffset.z,
            boxOffset.x,
            boxOffset.y,
            boxOffset.z
        ).expand(1 / 16D);
    }

    public static Pair<VolumeBox, EnumAddonSlot> getSelectingVolumeBoxAndSlot(PlayerEntity player,
                                                                              List<VolumeBox> volumeBoxes) {
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVector().multiply(4));
        VolumeBox bestVolumeBox = null;
        EnumAddonSlot bestSlot = null;
        double bestDist = Double.MAX_VALUE;

        for (VolumeBox volumeBox : volumeBoxes) {
            for (EnumAddonSlot slot : values()) {
                Optional<Vec3d> ray = slot.getBoundingBox(volumeBox).raycast(start, end);
                if (ray.isPresent()) {
                    double dist = ray.get().distanceTo(start);
                    if (bestDist > dist) {
                        bestDist = dist;
                        bestVolumeBox = volumeBox;
                        bestSlot = slot;
                    }
                }
            }
        }

        return Pair.of(bestVolumeBox, bestSlot);
    }
}
