/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.marker;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.lib.client.render.laser.LaserBoxRenderer;
import ct.buildcraft.lib.marker.MarkerConnection;
import ct.buildcraft.lib.misc.PositionUtil;
import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VolumeConnection extends MarkerConnection<VolumeConnection> {
    private static final double RENDER_SCALE = 1 / 16.05;

    private final Set<BlockPos> makeup = new HashSet<>();
    private final Box box = new Box();

    public static boolean tryCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
        if (canCreateConnection(subCache, from, to)) {
            VolumeConnection connection = new VolumeConnection(subCache);
            connection.makeup.add(from);
            connection.makeup.add(to);
            connection.createBox();
            subCache.addConnection(connection);
            return true;
        }
        return false;
    }

    public static boolean canCreateConnection(VolumeSubCache subCache, BlockPos from, BlockPos to) {
        Direction directOffset = PositionUtil.getDirectFacingOffset(from, to);
        if (directOffset == null) return false;
        for (int i = 1; i <= BCCoreConfig.markerMaxDistance; i++) {
            BlockPos offset = from.offset(directOffset.getNormal().multiply(i));
            if (offset.equals(to)) return true;
            if (subCache.hasLoadedOrUnloadedMarker(offset)) return false;
        }
        return false;
    }

    public VolumeConnection(VolumeSubCache subCache) {
        super(subCache);
    }

    public VolumeConnection(VolumeSubCache subCache, Collection<BlockPos> positions) {
        super(subCache);
        makeup.addAll(positions);
        createBox();
    }

    @Override
    public void removeMarker(BlockPos pos) {
        makeup.remove(pos);
        if (makeup.size() < 2) {
            // This connection will be removed by the sub-cache
            makeup.clear();
        }
        createBox();
    }

    public boolean addMarker(BlockPos pos) {
        if (canAddMarker(pos)) {
            makeup.add(pos);
            createBox();
            subCache.refreshConnection(this);
            return true;
        }
        return false;
    }

    public boolean canAddMarker(BlockPos to) {
        Set<Axis> taken = getConnectedAxis();
        for (BlockPos from : makeup) {
            Direction direct = PositionUtil.getDirectFacingOffset(from, to);
            if (direct != null && !taken.contains(direct.getAxis())) {
                return true;
            }
        }
        return !makeup.contains(to) && box.isCorner(to);
    }

    public boolean mergeWith(VolumeConnection other) {
        if (canMergeWith(other)) {
            makeup.addAll(other.makeup);
            other.makeup.clear();
            createBox();
            subCache.refreshConnection(other);
            subCache.refreshConnection(this);
            return true;
        }
        return false;
    }

    public boolean canMergeWith(VolumeConnection other) {
        EnumSet<Axis> us = getConnectedAxis();
        EnumSet<Axis> them = other.getConnectedAxis();
        if (us.size() != 1 || them.size() != 1) {
            return false;
        }
        if (us.equals(them)) {
            return false;
        }
        Set<Axis> blacklisted = EnumSet.copyOf(us);
        blacklisted.addAll(them);
        for (BlockPos from : makeup) {
            for (BlockPos to : other.makeup) {
                Direction offset = PositionUtil.getDirectFacingOffset(from, to);
                if (offset != null && !blacklisted.contains(offset.getAxis())) {
                    return true;
                }
            }
        }
        return false;
    }

    public EnumSet<Axis> getConnectedAxis() {
        EnumSet<Axis> taken = EnumSet.noneOf(Direction.Axis.class);
        for (BlockPos a : getMarkerPositions()) {
            for (BlockPos b : getMarkerPositions()) {
                Direction offset = PositionUtil.getDirectFacingOffset(a, b);
                if (offset != null) {
                    taken.add(offset.getAxis());
                }
            }
        }
        return taken;
    }

    @Override
    public Collection<BlockPos> getMarkerPositions() {
        return makeup;
    }

    private void createBox() {
        box.reset();
        for (BlockPos p : makeup) {
            box.extendToEncompass(p);
        }
    }

    public Box getBox() {
        return new Box(box.min(), box.max());
    }

    // ###########
    //
    // Rendering
    //
    // ###########

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInWorld(PoseStack pose, Matrix4f matrix) {
        LaserBoxRenderer.renderLaserBoxStatic(pose, matrix, box, BuildCraftLaserManager.MARKER_VOLUME_CONNECTED, true);
    }
}
