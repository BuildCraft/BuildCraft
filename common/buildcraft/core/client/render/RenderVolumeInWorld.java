/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock.Target.TargetUsedByMachine;
import buildcraft.core.marker.volume.Lock.Target.TargetUsedByMachine.EnumType;

public enum RenderVolumeInWorld implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    private static final double OFFSET_BY = 2 / 16.0;
    private static final double RENDER_SCALE = 1 / 16.0;
    private static final double RENDER_SCALE_HIGHLIGHT = 1 / 15.8;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        GlStateManager.enableBlend();

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ClientVolumeBoxes.INSTANCE.boxes.forEach(box -> {
            boolean isEditing = box.isEditingBy(player);
            double scale = isEditing ? RENDER_SCALE_HIGHLIGHT : RENDER_SCALE;

            LaserType laserType;
            if (!isEditing) {
                TargetUsedByMachine.EnumType type = box.getLockTargetsStream()//
                    .filter(TargetUsedByMachine.class::isInstance)//
                    .map(TargetUsedByMachine.class::cast)//
                    .map(target -> target.type)//
                    .findFirst().orElse(null);
                if (type == null) {
                    laserType = BuildCraftLaserManager.MARKER_VOLUME_CONNECTED;
                } else if (type == EnumType.STRIPES_READ) {
                    laserType = BuildCraftLaserManager.STRIPES_READ;
                } else {
                    laserType = BuildCraftLaserManager.STRIPES_WRITE;
                }
            } else {
                laserType = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
            }
            makeLaserBox(box.box, laserType, scale);

            Arrays.stream(box.box.laserData).forEach(data -> LaserRenderer_BC8.renderLaserDynamic(data, vb));

            // noinspection unchecked
            box.addons.values().forEach(addon -> ((IFastAddonRenderer<Addon>) addon.getRenderer())
                .renderAddonFast(addon, player, partialTicks, vb));
        });

        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
    }

    private static void makeLaserBox(Box box, LaserType type, double scale) {
        if (box.min().equals(box.lastMin) && box.max().equals(box.lastMax) && box.laserData != null) {
            return;
        }
        box.lastMin = box.min();
        box.lastMax = box.max();

        Vec3d min = new Vec3d(box.min());
        Vec3d max = new Vec3d(box.max()).add(VecUtil.VEC_ONE);
        Vec3d[][][] poses = new Vec3d[2][2][2];
        poses[0][0][0] = new Vec3d(min.xCoord, min.yCoord, min.zCoord);
        poses[0][0][1] = new Vec3d(min.xCoord, min.yCoord, max.zCoord);
        poses[0][1][0] = new Vec3d(min.xCoord, max.yCoord, min.zCoord);
        poses[0][1][1] = new Vec3d(min.xCoord, max.yCoord, max.zCoord);
        poses[1][0][0] = new Vec3d(max.xCoord, min.yCoord, min.zCoord);
        poses[1][0][1] = new Vec3d(max.xCoord, min.yCoord, max.zCoord);
        poses[1][1][0] = new Vec3d(max.xCoord, max.yCoord, min.zCoord);
        poses[1][1][1] = new Vec3d(max.xCoord, max.yCoord, max.zCoord);

        box.laserData = new LaserData_BC8[] {
            makeLaser(type, poses[0][0][0], poses[1][0][0], Axis.X, scale),
            makeLaser(type, poses[0][0][1], poses[1][0][1], Axis.X, scale),
            makeLaser(type, poses[0][1][0], poses[1][1][0], Axis.X, scale),
            makeLaser(type, poses[0][1][1], poses[1][1][1], Axis.X, scale),

            makeLaser(type, poses[0][0][0], poses[0][1][0], Axis.Y, scale),
            makeLaser(type, poses[0][0][1], poses[0][1][1], Axis.Y, scale),
            makeLaser(type, poses[1][0][0], poses[1][1][0], Axis.Y, scale),
            makeLaser(type, poses[1][0][1], poses[1][1][1], Axis.Y, scale),

            makeLaser(type, poses[0][0][0], poses[0][0][1], Axis.Z, scale),
            makeLaser(type, poses[0][1][0], poses[0][1][1], Axis.Z, scale),
            makeLaser(type, poses[1][0][0], poses[1][0][1], Axis.Z, scale),
            makeLaser(type, poses[1][1][0], poses[1][1][1], Axis.Z, scale)
        };
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3d min, Vec3d max, Axis axis, double scale) {
        switch (axis) {
            case X:
                min = new Vec3d(min.xCoord - 1 / 16D, min.yCoord, min.zCoord);
                max = new Vec3d(max.xCoord + 1 / 16D, max.yCoord, max.zCoord);
                break;
            case Y:
                min = new Vec3d(min.xCoord, min.yCoord - 1 / 16D, min.zCoord);
                max = new Vec3d(max.xCoord, max.yCoord + 1 / 16D, max.zCoord);
                break;
            case Z:
                min = new Vec3d(min.xCoord, min.yCoord, min.zCoord - 1 / 16D);
                max = new Vec3d(max.xCoord, max.yCoord, max.zCoord + 1 / 16D);
                break;
        }
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = min.add(new Vec3d(faceForMin.getDirectionVec()).scale(OFFSET_BY));
        Vec3d two = max.add(new Vec3d(faceForMax.getDirectionVec()).scale(OFFSET_BY));
        return new LaserData_BC8(type, one, two, scale, true, false, 0);
    }
}
