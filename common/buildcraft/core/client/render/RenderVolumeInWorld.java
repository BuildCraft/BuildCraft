/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

@SideOnly(Side.CLIENT)
public enum RenderVolumeInWorld implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    private static final double OFFSET_BY = 2 / 16.0;
    private static final double RENDER_SCALE = 1 / 16.0;
    private static final double RENDER_SCALE_HIGHLIGHT = 1 / 15.8;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        GlStateManager.enableBlend();

        BufferBuilder bb = Tessellator.getInstance().getBuffer();

        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

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
                laserType = BuildCraftLaserManager.MARKER_VOLUME_CONNECTED;
            }
            makeLaserBox(box.box, laserType, scale);

            Arrays.stream(box.box.laserData).forEach(data -> LaserRenderer_BC8.renderLaserDynamic(data, bb));

            // noinspection unchecked
            box.addons.values().forEach(addon ->
                ((IFastAddonRenderer<Addon>) addon.getRenderer()).renderAddonFast(addon, player, partialTicks, bb)
            );
        });

        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
    }

    private static void makeLaserBox(Box box, LaserType type, double scale) {
        BlockPos min = box.min();
        BlockPos max = box.max();

        if (min.equals(box.lastMin) && max.equals(box.lastMax) && box.laserData != null) {
            return;
        }

        List<LaserData_BC8> datas = new ArrayList<>();

        Vec3d[][][] vecs = new Vec3d[2][2][2];
        vecs[0][0][0] = new Vec3d(min);
        vecs[1][0][0] = new Vec3d(new BlockPos(max.getX(), min.getY(), min.getZ()));
        vecs[0][1][0] = new Vec3d(new BlockPos(min.getX(), max.getY(), min.getZ()));
        vecs[1][1][0] = new Vec3d(new BlockPos(max.getX(), max.getY(), min.getZ()));
        vecs[0][0][1] = new Vec3d(new BlockPos(min.getX(), min.getY(), max.getZ()));
        vecs[1][0][1] = new Vec3d(new BlockPos(max.getX(), min.getY(), max.getZ()));
        vecs[0][1][1] = new Vec3d(new BlockPos(min.getX(), max.getY(), max.getZ()));
        vecs[1][1][1] = new Vec3d(max);

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Vec3d offset = new Vec3d((16 * x) / 16D, (16 * y) / 16D, (16 * z) / 16D);
                    vecs[x][y][z] = vecs[x][y][z].add(offset);
                }
            }
        }

        datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X, scale));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X, scale));
        datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X, scale));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X, scale));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y, scale));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y, scale));
        datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y, scale));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y, scale));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z, scale));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z, scale));
        datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z, scale));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z, scale));

        box.laserData = datas.toArray(new LaserData_BC8[datas.size()]);
        box.lastMin = min;
        box.lastMax = max;
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3d min, Vec3d max, Axis axis, double scale) {
        switch (axis) {
            case X:
                min = new Vec3d(min.x - 1 / 16D, min.y, min.z);
                max = new Vec3d(max.x + 1 / 16D, max.y, max.z);
                break;
            case Y:
                min = new Vec3d(min.x, min.y - 1 / 16D, min.z);
                max = new Vec3d(max.x, max.y + 1 / 16D, max.z);
                break;
            case Z:
                min = new Vec3d(min.x, min.y, min.z - 1 / 16D);
                max = new Vec3d(max.x, max.y, max.z + 1 / 16D);
                break;
        }
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = min.add(new Vec3d(faceForMin.getDirectionVec()).scale(OFFSET_BY));
        Vec3d two = max.add(new Vec3d(faceForMax.getDirectionVec()).scale(OFFSET_BY));
        return new LaserData_BC8(type, one, two, scale, true, false, 0);
    }
}
