/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.MutableQuad;

public class LaserContext {
    public final Matrix4f matrix = new Matrix4f();
    private final Point3f point = new Point3f();
    private final Vector4f normal = new Vector4f();
    private final ILaserRenderer renderer;
    public final double length;
    private final boolean useNormalColour, drawBothSides;
    private final int minBlockLight;

    public LaserContext(ILaserRenderer renderer, LaserData_BC8 data, boolean useNormalColour, boolean isCullEnabled) {
        this.renderer = renderer;
        this.useNormalColour = useNormalColour;
        this.drawBothSides = isCullEnabled;
        this.minBlockLight = data.minBlockLight;
        Vec3d delta = data.start.subtract(data.end);
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        final double angleY, angleZ;

        double realLength = delta.lengthVector();
        length = realLength / data.scale;
        angleZ = Math.PI - Math.atan2(dz, dx);
        double rl_squared = realLength * realLength;
        double dy_dy = dy * dy;
        if (dx == 0 && dz == 0) {
            final double angle = Math.PI / 2;
            if (dy < 0) {
                angleY = angle;
            } else {
                angleY = -angle;
            }
        } else {
            dx = Math.sqrt(rl_squared - dy_dy);
            angleY = -Math.atan2(dy, dx);
        }

        // Matrix steps:
        // 1: rotate angles (Y) to make everything work
        // 2: rotate angles (Z) to make everything work
        // 3: scale it by the laser's scale
        // 4: translate forward by "start"

        matrix.setIdentity();
        Matrix4f holding = new Matrix4f();
        holding.setIdentity();

        // // Step 4
        Vector3f translation = new Vector3f();
        translation.x = (float) data.start.x;
        translation.y = (float) data.start.y;
        translation.z = (float) data.start.z;
        holding.setTranslation(translation);
        matrix.mul(holding);
        holding.setIdentity();

        // Step 3
        holding.m00 = (float) data.scale;
        holding.m11 = (float) data.scale;
        holding.m22 = (float) data.scale;
        matrix.mul(holding);
        holding.setIdentity();

        // Step 2
        holding.rotY((float) angleZ);
        matrix.mul(holding);
        holding.setIdentity();

        // Step 1
        holding.rotZ((float) angleY);
        matrix.mul(holding);
        holding.setIdentity();
    }

    @SideOnly(Side.CLIENT)
    public void setFaceNormal(double nx, double ny, double nz) {
        if (useNormalColour) {
            normal.x = (float) nx;
            normal.y = (float) ny;
            normal.z = (float) nz;
            normal.w = 0;
            matrix.transform(normal);
            n[0] = normal.x;
            n[1] = normal.y;
            n[2] = normal.z;
            diffuse = MutableQuad.diffuseLight(n[0], n[1], n[2]);
        }
    }

    private int index = 0;
    private final double[] x = { 0, 0, 0, 0 };
    private final double[] y = { 0, 0, 0, 0 };
    private final double[] z = { 0, 0, 0, 0 };
    private final double[] u = { 0, 0, 0, 0 };
    private final double[] v = { 0, 0, 0, 0 };
    private final int[] l = { 0, 0, 0, 0 };
    private final float[] n = { 0, 1, 0 };
    private float diffuse;

    @SideOnly(Side.CLIENT)
    public void addPoint(double xIn, double yIn, double zIn, double uIn, double vIn) {
        point.x = (float) xIn;
        point.y = (float) yIn;
        point.z = (float) zIn;
        matrix.transform(point);
        int lmap = LaserRenderer_BC8.computeLightmap(point.x, point.y, point.z, minBlockLight);
        x[index] = point.x;
        y[index] = point.y;
        z[index] = point.z;
        u[index] = uIn;
        v[index] = vIn;
        l[index] = lmap;
        index++;
        if (index == 4) {
            index = 0;
            vertex(0);
            vertex(1);
            vertex(2);
            vertex(3);
            if (drawBothSides) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
                diffuse = MutableQuad.diffuseLight(n[0], n[1], n[2]);
                vertex(3);
                vertex(2);
                vertex(1);
                vertex(0);
            }
            n[0] = 0;
            n[1] = 1;
            n[2] = 0;
        }
    }

    private void vertex(int i) {
        if (useNormalColour) {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], n[0], n[1], n[2], diffuse);
        } else {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], 0, 1, 0, 1);
        }
    }
}
