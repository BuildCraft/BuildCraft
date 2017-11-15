/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public enum EnumAddonSlot {
    EAST_UP_SOUTH(EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.POSITIVE),
    EAST_UP_NORTH(EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.NEGATIVE),
    EAST_DOWN_SOUTH(EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE),
    EAST_DOWN_NORTH(EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.NEGATIVE),
    WEST_UP_SOUTH(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.POSITIVE),
    WEST_UP_NORTH(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE, EnumFacing.AxisDirection.NEGATIVE),
    WEST_DOWN_SOUTH(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE),
    WEST_DOWN_NORTH(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.NEGATIVE);

    public static final EnumAddonSlot[] VALUES = values();

    public final Map<EnumFacing.Axis, EnumFacing.AxisDirection> directions = new EnumMap<>(EnumFacing.Axis.class);

    EnumAddonSlot(EnumFacing.AxisDirection x, EnumFacing.AxisDirection y, EnumFacing.AxisDirection z) {
        directions.put(EnumFacing.Axis.X, x);
        directions.put(EnumFacing.Axis.Y, y);
        directions.put(EnumFacing.Axis.Z, z);
    }

    public AxisAlignedBB getBoundingBox(VolumeBox volumeBox) {
        AxisAlignedBB aabb = volumeBox.box.getBoundingBox();
        Vec3d boxOffset = new Vec3d(
            directions.get(EnumFacing.Axis.X) == EnumFacing.AxisDirection.POSITIVE ? aabb.maxX : aabb.minX,
            directions.get(EnumFacing.Axis.Y) == EnumFacing.AxisDirection.POSITIVE ? aabb.maxY : aabb.minY,
            directions.get(EnumFacing.Axis.Z) == EnumFacing.AxisDirection.POSITIVE ? aabb.maxZ : aabb.minZ
        );
        return new AxisAlignedBB(
            boxOffset.xCoord,
            boxOffset.yCoord,
            boxOffset.zCoord,
            boxOffset.xCoord,
            boxOffset.yCoord,
            boxOffset.zCoord
        ).expandXyz(1 / 16D);
    }

    public static Pair<VolumeBox, EnumAddonSlot> getSelectingVolumeBoxAndSlot(EntityPlayer player,
                                                                              List<VolumeBox> volumeBoxes) {
        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d end = start.add(player.getLookVec().scale(4));
        VolumeBox bestVolumeBox = null;
        EnumAddonSlot bestSlot = null;
        double bestDist = Double.MAX_VALUE;

        for (VolumeBox volumeBox : volumeBoxes) {
            for (EnumAddonSlot slot : values()) {
                RayTraceResult ray = slot.getBoundingBox(volumeBox).calculateIntercept(start, end);
                if (ray != null) {
                    double dist = ray.hitVec.distanceTo(start);
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
