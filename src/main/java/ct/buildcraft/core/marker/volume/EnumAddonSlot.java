/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public AABB getBoundingBox(VolumeBox volumeBox) {
        AABB aabb = volumeBox.box.getBoundingBox();
        Vec3 boxOffset = new Vec3(
            directions.get(Direction.Axis.X) == Direction.AxisDirection.POSITIVE ? aabb.maxX : aabb.minX,
            directions.get(Direction.Axis.Y) == Direction.AxisDirection.POSITIVE ? aabb.maxY : aabb.minY,
            directions.get(Direction.Axis.Z) == Direction.AxisDirection.POSITIVE ? aabb.maxZ : aabb.minZ
        );
        return new AABB(
            boxOffset.x,
            boxOffset.y,
            boxOffset.z,
            boxOffset.x,
            boxOffset.y,
            boxOffset.z
        ).inflate(1 / 16D);
    }

    public static Pair<VolumeBox, EnumAddonSlot> getSelectingVolumeBoxAndSlot(Player player,
                                                                              List<VolumeBox> volumeBoxes) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(4));
        VolumeBox bestVolumeBox = null;
        EnumAddonSlot bestSlot = null;
        double bestDist = Double.MAX_VALUE;

        for (VolumeBox volumeBox : volumeBoxes) {
            for (EnumAddonSlot slot : values()) {
                Optional<Vec3> ray = slot.getBoundingBox(volumeBox).clip(start, end);
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
