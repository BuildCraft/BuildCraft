/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client.render;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;

public class RenderMarkerVolume extends TileEntitySpecialRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.2; // smaller than normal lasers

    public static final RenderMarkerVolume INSTANCE = new RenderMarkerVolume();

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    @Override
    public boolean isGlobalRenderer(TileMarkerVolume te) {
        return true;
    }

    @Override
    public void renderTileEntityAt(TileMarkerVolume marker, double tileX, double tileY, double tileZ, float partialTicks, int destroyStage) {
        if (marker == null || !marker.isShowingSignals()) return;

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("marker");
        Minecraft.getMinecraft().mcProfiler.startSection("volume");

        DetatchedRenderer.fromWorldOriginPre(Minecraft.getMinecraft().player, partialTicks);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        VolumeConnection volume = marker.getCurrentConnection();
        Set<Axis> taken = volume == null ? ImmutableSet.of() : volume.getConnectedAxis();

        Vec3d start = VecUtil.add(VEC_HALF, marker.getPos());
        for (EnumFacing face : EnumFacing.values()) {
            if (taken.contains(face.getAxis())) {
                continue;
            }
            Vec3d end = VecUtil.offset(start, face, BCCoreConfig.markerMaxDistance);
            renderLaser(start, end, face.getAxis());
        }

        RenderHelper.enableStandardItemLighting();
        DetatchedRenderer.fromWorldOriginPost();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderLaser(Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaserStatic(data);
    }

    private static Vec3d offset(Vec3d vec, EnumFacing face) {
        double by = 1 / 16.0;
        if (face == EnumFacing.DOWN) {
            return vec.addVector(0, -by, 0);
        } else if (face == EnumFacing.UP) {
            return vec.addVector(0, by, 0);
        } else if (face == EnumFacing.EAST) {
            return vec.addVector(by, 0, 0);
        } else if (face == EnumFacing.WEST) {
            return vec.addVector(-by, 0, 0);
        } else if (face == EnumFacing.SOUTH) {
            return vec.addVector(0, 0, by);
        } else {// North
            return vec.addVector(0, 0, -by);
        }
    }
}
