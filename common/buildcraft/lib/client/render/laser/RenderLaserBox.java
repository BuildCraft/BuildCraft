/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.function.TriFunction;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class RenderLaserBox {
    private static final double DEFAULT_SCALE = 1 / 16D;
    private static final LoadingCache<Args, List<LaserData_BC8>> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.SECONDS)
        .build(CacheLoader.from(RenderLaserBox::createData));

    private static List<LaserData_BC8> createData(Args args) {
        Vec3d min = new Vec3d(args.box.min()).add(args.center ? VecUtil.VEC_HALF : Vec3d.ZERO);
        Vec3d max = new Vec3d(args.box.max()).add(args.center ? VecUtil.VEC_HALF : VecUtil.VEC_ONE);
        Vec3d[][][] poses = new Vec3d[2][2][2];
        poses[0][0][0] = new Vec3d(min.xCoord, min.yCoord, min.zCoord);
        poses[0][0][1] = new Vec3d(min.xCoord, min.yCoord, max.zCoord);
        poses[0][1][0] = new Vec3d(min.xCoord, max.yCoord, min.zCoord);
        poses[0][1][1] = new Vec3d(min.xCoord, max.yCoord, max.zCoord);
        poses[1][0][0] = new Vec3d(max.xCoord, min.yCoord, min.zCoord);
        poses[1][0][1] = new Vec3d(max.xCoord, min.yCoord, max.zCoord);
        poses[1][1][0] = new Vec3d(max.xCoord, max.yCoord, min.zCoord);
        poses[1][1][1] = new Vec3d(max.xCoord, max.yCoord, max.zCoord);

        TriFunction<Vec3d, Vec3d, EnumFacing.Axis, LaserData_BC8> makeLaser = (minPos, maxPos, axis) ->
            new LaserData_BC8(
                args.laserType,
                minPos.add(new Vec3d(VecUtil.getFacing(axis, true).getDirectionVec()).scale(1 / 16D)),
                maxPos.add(new Vec3d(VecUtil.getFacing(axis, false).getDirectionVec()).scale(1 / 16D)),
                args.scale,
                false,
                false,
                0
            );
        return ImmutableList.of(
            makeLaser.apply(poses[0][0][0], poses[1][0][0], EnumFacing.Axis.X),
            makeLaser.apply(poses[0][0][1], poses[1][0][1], EnumFacing.Axis.X),
            makeLaser.apply(poses[0][1][0], poses[1][1][0], EnumFacing.Axis.X),
            makeLaser.apply(poses[0][1][1], poses[1][1][1], EnumFacing.Axis.X),

            makeLaser.apply(poses[0][0][0], poses[0][1][0], EnumFacing.Axis.Y),
            makeLaser.apply(poses[0][0][1], poses[0][1][1], EnumFacing.Axis.Y),
            makeLaser.apply(poses[1][0][0], poses[1][1][0], EnumFacing.Axis.Y),
            makeLaser.apply(poses[1][0][1], poses[1][1][1], EnumFacing.Axis.Y),

            makeLaser.apply(poses[0][0][0], poses[0][0][1], EnumFacing.Axis.Z),
            makeLaser.apply(poses[0][1][0], poses[0][1][1], EnumFacing.Axis.Z),
            makeLaser.apply(poses[1][0][0], poses[1][0][1], EnumFacing.Axis.Z),
            makeLaser.apply(poses[1][1][0], poses[1][1][1], EnumFacing.Axis.Z)
        );
    }

    private static List<LaserData_BC8> render(Box box,
                                              LaserData_BC8.LaserType laserType,
                                              double scale,
                                              boolean center) {
        return box == null || box.min() == null || box.max() == null
            ? Collections.emptyList()
            : CACHE.getUnchecked(new Args(box, laserType, scale, center));
    }

    private static List<LaserData_BC8> render(Box box,
                                              LaserData_BC8.LaserType laserType,
                                              boolean center) {
        return render(box, laserType, DEFAULT_SCALE, center);
    }

    @SuppressWarnings("unused")
    public static void renderStatic(Box box,
                                    LaserData_BC8.LaserType laserType,
                                    double scale,
                                    boolean center) {
        render(box, laserType, scale, center).forEach(LaserRenderer_BC8::renderLaserStatic);
    }

    @SuppressWarnings("unused")
    public static void renderStatic(Box box,
                                    LaserData_BC8.LaserType laserType,
                                    boolean center) {
        render(box, laserType, center).forEach(LaserRenderer_BC8::renderLaserStatic);
    }

    @SuppressWarnings("unused")
    public static void renderDynamic(Box box,
                                     LaserData_BC8.LaserType laserType,
                                     VertexBuffer vb,
                                     double scale,
                                     boolean center) {
        render(box, laserType, scale, center).forEach(data -> LaserRenderer_BC8.renderLaserDynamic(data, vb));
    }

    @SuppressWarnings("unused")
    public static void renderDynamic(Box box,
                                     LaserData_BC8.LaserType laserType,
                                     VertexBuffer vb,
                                     boolean center) {
        render(box, laserType, center).forEach(data -> LaserRenderer_BC8.renderLaserDynamic(data, vb));
    }

    @SuppressWarnings("WeakerAccess")
    private static class Args {
        public final Box box;
        public final LaserData_BC8.LaserType laserType;
        public final double scale;
        public final boolean center;

        private Args(Box box,
                     LaserData_BC8.LaserType laserType,
                     double scale,
                     boolean center) {
            this.box = box;
            this.laserType = laserType;
            this.scale = scale;
            this.center = center;
        }

        @Override
        public boolean equals(Object o) {
            return this == o ||
                o != null &&
                    getClass() == o.getClass() &&
                    Double.compare(((Args) o).scale, scale) == 0 &&
                    center == ((Args) o).center &&
                    box.equals(((Args) o).box) &&
                    laserType.equals(((Args) o).laserType);

        }
    }
}
