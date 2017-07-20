/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.*;
import java.util.Map;

public class MatrixUtil {
    /** Rotation map for gates */
    private static final Map<EnumFacing, Matrix4f> rotationMap;

    static {
        ImmutableMap.Builder<EnumFacing, Matrix4f> builder = ImmutableMap.builder();
        for (EnumFacing face : EnumFacing.VALUES) {
            Matrix4f mat = new Matrix4f();
            mat.setIdentity();

            if (face == EnumFacing.WEST) {
                builder.put(face, mat);
                continue;
            }
            mat.setTranslation(new Vector3f(0.5f, 0.5f, 0.5f));
            Matrix4f m2 = new Matrix4f();
            m2.setIdentity();

            if (face.getAxis() == Axis.Y) {
                AxisAngle4f axisAngle = new AxisAngle4f(0, 0, 1, (float) Math.PI * 0.5f * -face.getFrontOffsetY());
                m2.setRotation(axisAngle);
                mat.mul(m2);

                m2.setIdentity();
                m2.setRotation(new AxisAngle4f(1, 0, 0, (float) Math.PI * (1 + face.getFrontOffsetY() * 0.5f)));
                mat.mul(m2);
            } else {
                int ang;
                if (face == EnumFacing.EAST) ang = 2;
                else if (face == EnumFacing.NORTH) ang = 3;
                else ang = 1;
                AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, (float) Math.PI * 0.5f * ang);
                m2.setRotation(axisAngle);
                mat.mul(m2);
            }

            m2.setIdentity();
            m2.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
            mat.mul(m2);
            builder.put(face, mat);
        }
        rotationMap = builder.build();
    }

    /** Rotates towards the given face, assuming what you want to rotate from is WEST. */
    public static Matrix4f rotateTowardsFace(EnumFacing face) {
        return new Matrix4f(rotationMap.get(face));
    }

    /** Rotates towards the given face, from the specified face */
    public static Matrix4f rotateTowardsFace(EnumFacing from, EnumFacing to) {
        Matrix4f fromMatrix = new Matrix4f(rotateTowardsFace(from));
        // Because we want to do the opposite of what this does
        fromMatrix.invert();

        Matrix4f toMatrix = rotateTowardsFace(to);
        Matrix4f result = new Matrix4f(toMatrix);
        result.mul(fromMatrix);
        return result;
    }

    public static AxisAlignedBB multiply(AxisAlignedBB box, Matrix4f matrix) {
        Point3f min = new Point3f(new Point3d(box.minX, box.minY, box.minZ));
        Point3f max = new Point3f(new Point3d(box.maxX, box.maxY, box.maxZ));
        matrix.transform(min);
        matrix.transform(max);
        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public static AxisAlignedBB[] multiplyAll(AxisAlignedBB[] boxes, Matrix4f matrix) {
        AxisAlignedBB[] result = new AxisAlignedBB[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            result[i] = multiply(boxes[i], matrix);
        }
        return result;
    }
}
