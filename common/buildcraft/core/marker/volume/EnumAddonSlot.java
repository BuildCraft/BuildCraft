/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public AxisAlignedBB getBoundingBox(VolumeBox volumeBox) {
        AxisAlignedBB aabb = volumeBox.box.getBoundingBox();
        Vector3d boxOffset = new Vector3d(
                directions.get(Direction.Axis.X) == Direction.AxisDirection.POSITIVE ? aabb.maxX : aabb.minX,
                directions.get(Direction.Axis.Y) == Direction.AxisDirection.POSITIVE ? aabb.maxY : aabb.minY,
                directions.get(Direction.Axis.Z) == Direction.AxisDirection.POSITIVE ? aabb.maxZ : aabb.minZ
        );
        return new AxisAlignedBB(
                boxOffset.x,
                boxOffset.y,
                boxOffset.z,
                boxOffset.x,
                boxOffset.y,
                boxOffset.z
        ).inflate(1 / 16D);
    }

    public static Pair<VolumeBox, EnumAddonSlot> getSelectingVolumeBoxAndSlot(PlayerEntity player, List<VolumeBox> volumeBoxes) {
        Vector3d start = player.position().add(0, player.getEyeHeight(), 0);
        Vector3d end = start.add(player.getLookAngle().scale(4));
        VolumeBox bestVolumeBox = null;
        EnumAddonSlot bestSlot = null;
        double bestDist = Double.MAX_VALUE;

        for (VolumeBox volumeBox : volumeBoxes) {
            for (EnumAddonSlot slot : values()) {
                Optional<Vector3d> ray = slot.getBoundingBox(volumeBox).clip(start, end);
//                if (ray != null)
                if (ray.isPresent()) {
//                    double dist = ray.hitVec.distanceTo(start);
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
