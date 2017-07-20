/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            makeLaserBox(
                    box.box,
                    box.isEditingBy(player) ?
                            BuildCraftLaserManager.MARKER_VOLUME_SIGNAL :
                            box.getLockTargetsStream().anyMatch(Lock.Target.TargetUsedByMachine.class::isInstance) ?
                                    box.getLockTargetsStream()
                                            .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                                            .map(Lock.Target.TargetUsedByMachine.class::cast)
                                            .map(target -> target.type)
                                            .filter(Objects::nonNull)
                                            .findFirst()
                                            .orElse(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE)
                                            .laserType :
                                    BuildCraftLaserManager.MARKER_VOLUME_CONNECTED,
                    box.isEditingBy(player) ? RENDER_SCALE_HIGHLIGHT : RENDER_SCALE
            );

            Arrays.stream(box.box.laserData).forEach(data -> LaserRenderer_BC8.renderLaserDynamic(data, vb));

            // noinspection unchecked
            box.addons.values().forEach(addon ->
                ((IFastAddonRenderer<Addon>) addon.getRenderer()).renderAddonFast(addon, player, partialTicks, vb)
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

        datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X, scale, false));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X, scale, true));
        datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X, scale, false));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X, scale, true));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y, scale, false));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y, scale, true));
        datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y, scale, false));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y, scale, true));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z, scale, false));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z, scale, true));
        datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z, scale, false));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z, scale, true));

        box.laserData = datas.toArray(new LaserData_BC8[datas.size()]);
        box.lastMin = min;
        box.lastMax = max;
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3d min, Vec3d max, Axis axis, double scale, boolean second) {
        switch (axis) {
            case X:
                if (second) {
                    min = new Vec3d(min.xCoord - 1 / 16D, min.yCoord, min.zCoord);
                    max = new Vec3d(max.xCoord + 1 / 16D, max.yCoord, max.zCoord);
                } else {
                    min = new Vec3d(min.xCoord - 1 / 16D, min.yCoord, min.zCoord);
                    max = new Vec3d(max.xCoord + 1 / 16D, max.yCoord, max.zCoord);
                }
                break;
            case Y:
                min = new Vec3d(min.xCoord, min.yCoord - 1 / 16D, min.zCoord);
                max = new Vec3d(max.xCoord, max.yCoord + 1 / 16D, max.zCoord);
                break;
            case Z:
                if (second) {
                    min = new Vec3d(min.xCoord, min.yCoord, min.zCoord - 1 / 16D);
                    max = new Vec3d(max.xCoord, max.yCoord, max.zCoord + 1 / 16D);
                } else {
                    min = new Vec3d(min.xCoord, min.yCoord, min.zCoord - 1 / 16D);
                    max = new Vec3d(max.xCoord, max.yCoord, max.zCoord + 1 / 16D);
                }
                break;
        }
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = min.add(new Vec3d(faceForMin.getDirectionVec()).scale(OFFSET_BY));
        Vec3d two = max.add(new Vec3d(faceForMax.getDirectionVec()).scale(OFFSET_BY));
        return new LaserData_BC8(type, one, two, scale, true, false, 0);
    }
}
