/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client.render;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.tile.TileMarkerVolume;

public class RenderMarkerVolume extends TileEntitySpecialRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.2; // smaller than normal lasers

    public static final RenderMarkerVolume INSTANCE = new RenderMarkerVolume();

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isGlobalRenderer(TileMarkerVolume te) {
        return true;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(TileMarkerVolume marker, double tileX, double tileY, double tileZ, float partialTicks, int destroyStage, float alpha) {
        if (marker == null || !marker.isShowingSignals()) return;

        MinecraftClient.getInstance().getProfiler().push("bc");
        MinecraftClient.getInstance().getProfiler().push("marker");
        MinecraftClient.getInstance().getProfiler().push("volume");

        DetachedRenderer.fromWorldOriginPre(MinecraftClient.getInstance().player, partialTicks);
        RenderHelper.disableStandardItemLighting();
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

        VolumeConnection volume = marker.getCurrentConnection();
        Set<Axis> taken = volume == null ? ImmutableSet.of() : volume.getConnectedAxis();

        Vec3d start = VecUtil.add(VEC_HALF, marker.getPos());
        for (Direction face : Direction.values()) {
            if (taken.contains(face.getAxis())) {
                continue;
            }
            Vec3d end = VecUtil.offset(start, face, BCCoreConfig.markerMaxDistance);
            renderLaser(start, end, face.getAxis());
        }

        RenderHelper.enableStandardItemLighting();
        DetachedRenderer.fromWorldOriginPost();

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    private static void renderLaser(Vec3d min, Vec3d max, Axis axis) {
        Direction faceForMin = VecUtil.getFacing(axis, true);
        Direction faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaserStatic(data);
    }

    private static Vec3d offset(Vec3d vec, Direction face) {
        double by = 1 / 16.0;
        if (face == Direction.DOWN) {
            return vec.add(0, -by, 0);
        } else if (face == Direction.UP) {
            return vec.add(0, by, 0);
        } else if (face == Direction.EAST) {
            return vec.add(by, 0, 0);
        } else if (face == Direction.WEST) {
            return vec.add(-by, 0, 0);
        } else if (face == Direction.SOUTH) {
            return vec.add(0, 0, by);
        } else {// North
            return vec.add(0, 0, -by);
        }
    }
}
