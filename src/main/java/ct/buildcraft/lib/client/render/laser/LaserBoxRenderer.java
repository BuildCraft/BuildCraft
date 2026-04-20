/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.misc.data.Box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaserBoxRenderer {
    private static final double RENDER_SCALE = 1 / 16.05;

    public static void renderLaserBoxStatic(PoseStack pose, Matrix4f matrix, Box box, LaserType type, boolean center) {
//    	BCLog.logger.debug("LaserBoxRenderer.renderLaserBoxStatic:call unimplemented method");
        if (box == null || box.min() == null || box.max() == null) {
            return;
        }
        
        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserStatic(pose, matrix, data);
        }
    }

    public static void renderLaserBoxDynamic(Box box, LaserType type, Matrix4f pose, Matrix3f normal, VertexConsumer bb, boolean center) {
        if (box == null || box.min() == null || box.max() == null) {
            return;
        }

        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserDynamic(pose, normal, data, bb);//TODO temporary
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

        Vec3 min = Vec3.atLowerCornerOf(box.min()).add(center ? VecUtil.VEC_HALF : Vec3.ZERO);
        Vec3 max = Vec3.atLowerCornerOf(box.max()).add(center ? VecUtil.VEC_HALF : VecUtil.VEC_ONE);

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
        Vec3 one = min.add(Vec3.atLowerCornerOf(faceForMin.getNormal()).scale(1 / 16D));
        Vec3 two = max.add(Vec3.atLowerCornerOf(faceForMax.getNormal()).scale(1 / 16D));
        return new LaserData_BC8(type, one, two, RENDER_SCALE, true);//TODO temporary
    }

}
