/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;

public class MatrixUtil {
    /** Rotation map for gates */
    private static final Map<Direction, Matrix4f> rotationMap;

    static {
        ImmutableMap.Builder<Direction, Matrix4f> builder = ImmutableMap.builder();
        for (Direction face : Direction.VALUES) {
            Matrix4f mat = new Matrix4f();
            mat.identity();

            if (face == Direction.WEST) {
                builder.put(face, mat);
                continue;
            }
            mat.setTranslation(new Vector3f(0.5f, 0.5f, 0.5f));
            Matrix4f m2 = new Matrix4f();
            m2.identity();

            if (face.getAxis() == Axis.Y) {
                AxisAngle4f axisAngle = new AxisAngle4f(0, 0, 1, (float) Math.PI * 0.5f * -face.getStepY());
                m2.rotate(axisAngle);
                mat.mul(m2);

                m2.identity();
                m2.rotate(new AxisAngle4f(1, 0, 0, (float) Math.PI * (1 + face.getStepY() * 0.5f)));
                mat.mul(m2);
            } else {
                int ang;
                if (face == Direction.EAST) ang = 2;
                else if (face == Direction.NORTH) ang = 3;
                else ang = 1;
                AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, (float) Math.PI * 0.5f * ang);
                m2.rotate(axisAngle);
                mat.mul(m2);
            }

            m2.identity();
            m2.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
            mat.mul(m2);
            builder.put(face, mat);
        }
        rotationMap = builder.build();
    }

    /** Rotates towards the given face, assuming what you want to rotate from is WEST. */
    public static Matrix4f rotateTowardsFace(Direction face) {
        return new Matrix4f(rotationMap.get(face));
    }

    /** Rotates towards the given face, from the specified face */
    public static Matrix4f rotateTowardsFace(Direction from, Direction to) {
        Matrix4f fromMatrix = new Matrix4f(rotateTowardsFace(from));
        // Because we want to do the opposite of what this does
        fromMatrix.invert();

        Matrix4f toMatrix = rotateTowardsFace(to);
        Matrix4f result = new Matrix4f(toMatrix);
        result.mul(fromMatrix);
        return result;
    }

    public static AABB multiply(AABB box, Matrix4f matrix) {
        Vector3f min3f = new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ);
        Vector3f max3f = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);
        Vector4f min4f = new Vector4f(min3f.x, min3f.y, min3f.z, 1);
        Vector4f max4f = new Vector4f(max3f.x, max3f.y, max3f.z, 1);
        matrix.transform(min4f);
        matrix.transform(max4f);
        return new AABB(min4f.x, min4f.y, min4f.z, max4f.x, max4f.y, max4f.z);
    }

    public static AABB[] multiplyAll(AABB[] boxes, Matrix4f matrix) {
        AABB[] result = new AABB[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            result[i] = multiply(boxes[i], matrix);
        }
        return result;
    }
}
