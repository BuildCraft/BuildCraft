/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model;



import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;

/** Provides various utilities for creating {@link MutableQuad} out of various position information, such as a single
 * face of a cuboid. */
public class ModelUtil {
    /** Mutable class for holding the current {@link #minU}, {@link #maxU}, {@link #minV} and {@link #maxV} of a
     * face. */
    public static class UvFaceData {
        private static final UvFaceData DEFAULT = new UvFaceData(0, 0, 1, 1);

        public float minU, maxU, minV, maxV;

        public UvFaceData() {}

        public UvFaceData(UvFaceData from) {
            this.minU = from.minU;
            this.maxU = from.maxU;
            this.minV = from.minV;
            this.maxV = from.maxV;
        }

        public static UvFaceData from16(double minU, double minV, double maxU, double maxV) {
            return new UvFaceData(minU / 16.0, minV / 16.0, maxU / 16.0, maxV / 16.0);
        }

        public static UvFaceData from16(int minU, int minV, int maxU, int maxV) {
            return new UvFaceData(minU / 16f, minV / 16f, maxU / 16f, maxV / 16f);
        }

        public UvFaceData(float uMin, float vMin, float uMax, float vMax) {
            this.minU = uMin;
            this.maxU = uMax;
            this.minV = vMin;
            this.maxV = vMax;
        }

        public UvFaceData(double minU, double minV, double maxU, double maxV) {
            this((float) minU, (float) minV, (float) maxU, (float) maxV);
        }

        public UvFaceData andSub(UvFaceData sub) {
            float size_u = maxU - minU;
            float size_v = maxV - minV;

            float min_u = minU + sub.minU * size_u;
            float min_v = minV + sub.minV * size_v;
            float max_u = minU + sub.maxU * size_u;
            float max_v = minV + sub.maxV * size_v;

            return new UvFaceData(min_u, min_v, max_u, max_v);
        }

        public UvFaceData inParent(UvFaceData parent) {
            return parent.andSub(this);
        }

        @Override
        public String toString() {
            return "[ " + minU * 16 + ", " + minV * 16 + ", " + maxU * 16 + ", " + maxV * 16 + " ]";
        }
    }

    public static class TexturedFace {
        public TextureAtlasSprite sprite;
        public UvFaceData faceData = new UvFaceData();
    }

    public static MutableQuad createFace(Direction face, Vector3f a, Vector3f b, Vector3f c, Vector3f d, UvFaceData uvs) {
        MutableQuad quad = new MutableQuad(-1, face);
        if (uvs == null) {
            uvs = UvFaceData.DEFAULT;
        }
        if (face == null || shouldInvertForRender(face)) {
            quad.vertex_0.positionv(a).texf(uvs.minU, uvs.minV);
            quad.vertex_1.positionv(b).texf(uvs.minU, uvs.maxV);
            quad.vertex_2.positionv(c).texf(uvs.maxU, uvs.maxV);
            quad.vertex_3.positionv(d).texf(uvs.maxU, uvs.minV);
        } else {
            quad.vertex_3.positionv(a).texf(uvs.minU, uvs.minV);
            quad.vertex_2.positionv(b).texf(uvs.minU, uvs.maxV);
            quad.vertex_1.positionv(c).texf(uvs.maxU, uvs.maxV);
            quad.vertex_0.positionv(d).texf(uvs.maxU, uvs.minV);
        }
        return quad;
    }

    public static MutableQuad createFace(Direction face, Vector3f[] points, UvFaceData uvs) {
        return createFace(face, points[0], points[1], points[2], points[3], uvs);
    }

    public static MutableQuad createFace(Direction face, Vector3f center, Vector3f radius, UvFaceData uvs) {
        Vector3f[] points = getPointsForFace(face, center, radius);
        return createFace(face, points, uvs).normalf(
            face.getStepX(), face.getStepY(), face.getStepZ()
        );
    }

    public static MutableQuad createInverseFace(Direction face, Vector3f center, Vector3f radius, UvFaceData uvs) {
        return createFace(face, center, radius, uvs).copyAndInvertNormal();
    }

    public static MutableQuad[] createDoubleFace(Direction face, Vector3f center, Vector3f radius, UvFaceData uvs) {
        MutableQuad norm = createFace(face, center, radius, uvs);
        return new MutableQuad[] { norm, norm.copyAndInvertNormal() };
    }

    public static void mapBoxToUvs(AABB box, Direction side, UvFaceData uvs) {
        // TODO: Fix these!
        switch (side) {
            case WEST: /* -X */ {
                uvs.minU = (float) box.minZ;
                uvs.maxU = (float) box.maxZ;
                uvs.minV = 1 - (float) box.maxY;
                uvs.maxV = 1 - (float) box.minY;
                return;
            }
            case EAST: /* +X */ {
                uvs.minU = 1 - (float) box.minZ;
                uvs.maxU = 1 - (float) box.maxZ;
                uvs.minV = 1 - (float) box.maxY;
                uvs.maxV = 1 - (float) box.minY;
                return;
            }
            case DOWN: /* -Y */ {
                uvs.minU = (float) box.minX;
                uvs.maxU = (float) box.maxX;
                uvs.minV = 1 - (float) box.maxZ;
                uvs.maxV = 1 - (float) box.minZ;
                return;
            }
            case UP: /* +Y */ {
                uvs.minU = (float) box.minX;
                uvs.maxU = (float) box.maxX;
                uvs.minV = (float) box.maxZ;
                uvs.maxV = (float) box.minZ;
                return;
            }
            case NORTH: /* -Z */ {
                uvs.minU = 1 - (float) box.minX;
                uvs.maxU = 1 - (float) box.maxX;
                uvs.minV = 1 - (float) box.maxY;
                uvs.maxV = 1 - (float) box.minY;
                return;
            }
            case SOUTH: /* +Z */ {
                uvs.minU = (float) box.minX;
                uvs.maxU = (float) box.maxX;
                uvs.minV = 1 - (float) box.maxY;
                uvs.maxV = 1 - (float) box.minY;
                return;
            }
            default: {
                throw new IllegalStateException("Unknown Direction " + side);
            }
        }
    }

    public static Vector3f[] getPointsForFace(Direction face, Vector3f center, Vector3f radius) {
    	Vector3f centerOfFace = center.copy();
    	Vector3f faceAdd = new Vector3f(
            face.getStepX() * radius.x(), face.getStepY() * radius.y(), face.getStepZ() * radius.z()
        );
        centerOfFace.add(faceAdd);
        Vector3f faceRadius = radius.copy();
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            faceRadius.sub(faceAdd);
        } else {
            faceRadius.add(faceAdd);
        }
        return getPoints(centerOfFace, faceRadius);
    }

    public static Vector3f[] getPoints(Vector3f centerFace, Vector3f faceRadius) {
        Vector3f[] array = { centerFace.copy(), centerFace.copy(), centerFace.copy(), centerFace.copy() };
        array[0].add(addOrNegate(faceRadius, false, false));
        array[1].add(addOrNegate(faceRadius, false, true));
        array[2].add(addOrNegate(faceRadius, true, true));
        array[3].add(addOrNegate(faceRadius, true, false));
        return array;
    }

    public static Vector3f addOrNegate(Vector3f coord, boolean u, boolean v) {
        boolean zisv = coord.x() != 0 && coord.y() == 0;
        float x = coord.x() * (u ? 1 : -1);
        float y = coord.y() * (v ? -1 : 1);
        float z = coord.z() * (zisv ? (v ? -1 : 1) : (u ? 1 : -1));
        Vector3f neg = new Vector3f(x, y, z);
        return neg;
    }

    public static boolean shouldInvertForRender(Direction face) {
        boolean flip = face.getAxisDirection() == AxisDirection.NEGATIVE;
        if (face.getAxis() == Axis.Z) flip = !flip;
        return flip;
    }

    public static Direction faceForRender(Direction face) {
        if (shouldInvertForRender(face)) return face.getOpposite();
        return face;
    }
}
