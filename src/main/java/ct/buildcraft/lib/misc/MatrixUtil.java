/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;

public class MatrixUtil {
    /** Rotation map for gates */
    private static final Map<Direction, Matrix4f> rotationMap;

    static {
        ImmutableMap.Builder<Direction, Matrix4f> builder = ImmutableMap.builder();
        for (Direction face : Direction.values()) {
            Matrix4f mat = new Matrix4f();
            mat.setIdentity();

            if (face == Direction.WEST) {
                builder.put(face, mat);
                continue;
            }
            mat.setTranslation(0.5f, 0.5f, 0.5f);
            Matrix4f m2 = new Matrix4f();
            m2.setIdentity();

            if (face.getAxis() == Axis.Y) {
            	Quaternion quaternion = new Quaternion(new Vector3f(0, 0, 1), (float) Math.PI * 0.5f * -face.getStepY(), false);
                m2.multiply(quaternion);
                mat.multiply(m2);

                m2.setIdentity();
                m2.multiply(new Quaternion(new Vector3f(1, 0, 0), (float) Math.PI * (1 + face.getStepY() * 0.5f), false));
                mat.multiply(m2);
            } else {
                int ang;
                if (face == Direction.EAST) ang = 2;
                else if (face == Direction.NORTH) ang = 3;
                else ang = 1;
                Quaternion quaternion = new Quaternion(new Vector3f(0, 1, 0), (float) Math.PI * 0.5f * ang, false);
                m2.multiply(quaternion);
                mat.multiply(m2);
            }

            m2.setIdentity();
            m2.setTranslation(-0.5f, -0.5f, -0.5f);
            mat.multiply(m2);
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
        result.multiply(fromMatrix);
        return result;
    }

    public static AABB multiply(AABB box, Matrix4f matrix) {
        Vector4f min = new Vector4f((float)box.minX, (float)box.minY, (float)box.minZ, 1);
        Vector4f max = new Vector4f((float)box.maxX, (float)box.maxY, (float)box.maxZ, 1);
        
        min.transform(matrix);
        max.transform(matrix);;
        return new AABB(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    public static AABB[] multiplyAll(AABB[] boxes, Matrix4f matrix) {
        AABB[] result = new AABB[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            result[i] = multiply(boxes[i], matrix);
        }
        return result;
    }
}
