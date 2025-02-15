/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LaserBoxRenderer {
    private static final double RENDER_SCALE = 1 / 16.05;

    public static void renderLaserBoxStatic(Box box, LaserType type, PoseStack.Pose modelViewMatrix, boolean center) {
        if (box == null || box.min() == null || box.max() == null) {
            return;
        }

        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
//            LaserRenderer_BC8.renderLaserStatic(data);
            LaserRenderer_BC8.renderLaserStatic(data, modelViewMatrix);
        }
    }

    public static void renderLaserBoxDynamic(Box box, LaserType type, PoseStack.Pose pose, VertexConsumer bb, boolean center) {
        if (box == null || box.min() == null || box.max() == null) {
            return;
        }

        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserDynamic(data, pose, bb);
        }
    }

    private static void makeLaserBox(Box box, LaserType type, boolean center) {
        if (box.min().equals(box.lastMin) && box.max().equals(box.lastMax) && box.lastType == type
                && box.laserData != null) {
            return;
        }

        boolean renderX = center ? box.size().getX() > 1 : true;
        boolean renderY = center ? box.size().getY() > 1 : true;
        boolean renderZ = center ? box.size().getZ() > 1 : true;

        Vec3 min = new Vec3(box.min().getX(), box.min().getY(), box.min().getZ()).add(center ? VecUtil.VEC_HALF : Vec3.ZERO);
        Vec3 max = new Vec3(box.max().getX(), box.max().getY(), box.max().getZ()).add(center ? VecUtil.VEC_HALF : VecUtil.VEC_ONE);

        List<LaserData_BC8> datas = new ArrayList<>();

        Vec3[][][] vecs = new Vec3[2][2][2];
        vecs[0][0][0] = new Vec3(min.x, min.y, min.z);
        vecs[1][0][0] = new Vec3(max.x, min.y, min.z);
        vecs[0][1][0] = new Vec3(min.x, max.y, min.z);
        vecs[1][1][0] = new Vec3(max.x, max.y, min.z);
        vecs[0][0][1] = new Vec3(min.x, min.y, max.z);
        vecs[1][0][1] = new Vec3(max.x, min.y, max.z);
        vecs[0][1][1] = new Vec3(min.x, max.y, max.z);
        vecs[1][1][1] = new Vec3(max.x, max.y, max.z);

        if (renderX) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X));
            if (renderY) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X));
                if (renderZ) {
                    datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X));
                }
            }
            if (renderZ) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X));
            }
        }

        if (renderY) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y));
            if (renderX) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y));
                if (renderZ) {
                    datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y));
                }
            }
            if (renderZ) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y));
            }
        }

        if (renderZ) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z));
            if (renderX) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z));
                if (renderY) {
                    datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z));
                }
            }
            if (renderY) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z));
            }
        }

        box.laserData = datas.toArray(new LaserData_BC8[0]);
        box.lastMin = box.min();
        box.lastMax = box.max();
        box.lastType = type;
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3 min, Vec3 max, Axis axis) {
        Direction faceForMin = VecUtil.getFacing(axis, true);
        Direction faceForMax = VecUtil.getFacing(axis, false);
        Vec3 one = min.add(new Vec3(faceForMin.getNormal().getX(), faceForMin.getNormal().getY(), faceForMin.getNormal().getZ()).scale(1 / 16D));
        Vec3 two = max.add(new Vec3(faceForMax.getNormal().getX(), faceForMax.getNormal().getY(), faceForMax.getNormal().getZ()).scale(1 / 16D));
        return new LaserData_BC8(type, one, two, RENDER_SCALE);
    }
}
