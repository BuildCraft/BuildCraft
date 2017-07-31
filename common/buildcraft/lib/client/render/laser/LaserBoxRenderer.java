/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

@SideOnly(Side.CLIENT)
public class LaserBoxRenderer {
    private static final double RENDER_SCALE = 1 / 16.05;

    public static void renderLaserBoxStatic(Box box, LaserType type, boolean center) {
        if (box == null || box.min() == null || box.max() == null) return;

        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserStatic(data);
        }
    }

    public static void renderLaserBoxDynamic(Box box, LaserType type, BufferBuilder bb, boolean center) {
        if (box == null || box.min() == null || box.max() == null) return;

        makeLaserBox(box, type, center);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserDynamic(data, bb);
        }
    }

    private static void makeLaserBox(Box box, LaserType type, boolean center) {
        if (box.min().equals(box.lastMin) && box.max().equals(box.lastMax) && box.laserData != null) {
            return;
        }

        int sizeX = box.size().getX();
        int sizeY = box.size().getY();
        int sizeZ = box.size().getZ();

        Vec3d min = new Vec3d(box.min()).add(center ? VecUtil.VEC_HALF : Vec3d.ZERO);
        Vec3d max = new Vec3d(box.max()).add(center ? VecUtil.VEC_HALF : VecUtil.VEC_ONE);

        List<LaserData_BC8> datas = new ArrayList<>();

        Vec3d[][][] vecs = new Vec3d[2][2][2];
        vecs[0][0][0] = new Vec3d(min.x, min.y, min.z);
        vecs[1][0][0] = new Vec3d(max.x, min.y, min.z);
        vecs[0][1][0] = new Vec3d(min.x, max.y, min.z);
        vecs[1][1][0] = new Vec3d(max.x, max.y, min.z);
        vecs[0][0][1] = new Vec3d(min.x, min.y, max.z);
        vecs[1][0][1] = new Vec3d(max.x, min.y, max.z);
        vecs[0][1][1] = new Vec3d(min.x, max.y, max.z);
        vecs[1][1][1] = new Vec3d(max.x, max.y, max.z);

        if (sizeX > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X));
            if (sizeY > 1) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X));
                if (sizeZ > 1) {
                    datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X));
                }
            }
            if (sizeZ > 1) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X));
            }
        }

        if (sizeY > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y));
            if (sizeX > 1) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y));
                if (sizeZ > 1) {
                    datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y));
                }
            }
            if (sizeZ > 1) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y));
            }
        }

        if (box.size().getZ() > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z));
            if (sizeX > 1) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z));
                if (sizeY > 0) {
                    datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z));
                }
            }
            if (sizeY > 0) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z));
            }
        }

        box.laserData = datas.toArray(new LaserData_BC8[0]);
        box.lastMin = box.min();
        box.lastMax = box.max();
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = min.add(new Vec3d(faceForMin.getDirectionVec()).scale(1 / 16D));
        Vec3d two = max.add(new Vec3d(faceForMax.getDirectionVec()).scale(1 / 16D));
        return new LaserData_BC8(type, one, two, RENDER_SCALE);
    }

}
