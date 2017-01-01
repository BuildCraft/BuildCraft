package buildcraft.lib.client.model;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

/** Provides various utilities for creating {@link MutableQuad} out of various position information, such as a single
 * face of a cuboid. */
public class ModelUtil {
    /** Mutable class for holding the current {@link #uMin}, {@link #uMax}, {@link #vMin} and {@link #vMax} of a
     * face. */
    public static class UvFaceData {
        private static final UvFaceData DEFAULT = new UvFaceData();

        public float uMin, uMax, vMin, vMax;

        public UvFaceData() {}

        public UvFaceData(UvFaceData from) {
            this.uMin = from.uMin;
            this.uMax = from.uMax;
            this.vMin = from.vMin;
            this.vMax = from.vMax;
        }

        public UvFaceData(float uMin, float uMax, float vMin, float vMax) {
            this.uMin = uMin;
            this.uMax = uMax;
            this.vMin = vMin;
            this.vMax = vMax;
        }
    }

    public static MutableQuad createFace(EnumFacing face, Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d, UvFaceData uvs) {
        MutableQuad mutable = new MutableQuad(-1, face);
        if (uvs == null) {
            uvs = UvFaceData.DEFAULT;
        }
        if (face == null || shouldInvertForRender(face)) {
            mutable.getVertex(0).positionv(a).texf(uvs.uMin, uvs.vMin);
            mutable.getVertex(1).positionv(b).texf(uvs.uMin, uvs.vMax);
            mutable.getVertex(2).positionv(c).texf(uvs.uMax, uvs.vMax);
            mutable.getVertex(3).positionv(d).texf(uvs.uMax, uvs.vMin);
        } else {
            mutable.getVertex(3).positionv(a).texf(uvs.uMin, uvs.vMin);
            mutable.getVertex(2).positionv(b).texf(uvs.uMin, uvs.vMax);
            mutable.getVertex(1).positionv(c).texf(uvs.uMax, uvs.vMax);
            mutable.getVertex(0).positionv(d).texf(uvs.uMax, uvs.vMin);
        }
        return mutable;
    }

    public static <T extends Tuple3f> MutableQuad createFace(EnumFacing face, T[] points, UvFaceData uvs) {
        return createFace(face, points[0], points[1], points[2], points[3], uvs);
    }

    public static MutableQuad createFace(EnumFacing face, Tuple3f center, Tuple3f radius, UvFaceData uvs) {
        Point3f[] points = getPointsForFace(face, center, radius);
        return createFace(face, points, uvs).normalf(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public static MutableQuad createInverseFace(EnumFacing face, Tuple3f center, Tuple3f radius, UvFaceData uvs) {
        return createFace(face, center, radius, uvs).invertNormal();
    }

    public static MutableQuad[] createDoubleFace(EnumFacing face, Tuple3f center, Tuple3f radius, UvFaceData uvs) {
        MutableQuad norm = createFace(face, center, radius, uvs);
        return new MutableQuad[] { norm, new MutableQuad(norm).invertNormal() };
    }

    public static Point3f[] getPointsForFace(EnumFacing face, Tuple3f center, Tuple3f radius) {
        Point3f centerOfFace = new Point3f(center);
        Point3f faceAdd = new Point3f(face.getFrontOffsetX() * radius.x, face.getFrontOffsetY() * radius.y, face.getFrontOffsetZ() * radius.z);
        centerOfFace.add(faceAdd);
        Vector3f faceRadius = new Vector3f(radius);
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            faceRadius.sub(faceAdd);
        } else {
            faceRadius.add(faceAdd);
        }
        return getPoints(centerOfFace, faceRadius);
    }

    public static Point3f[] getPoints(Point3f centerFace, Tuple3f faceRadius) {
        Point3f[] array = { new Point3f(centerFace), new Point3f(centerFace), new Point3f(centerFace), new Point3f(centerFace) };
        array[0].add(addOrNegate(faceRadius, false, false));
        array[1].add(addOrNegate(faceRadius, false, true));
        array[2].add(addOrNegate(faceRadius, true, true));
        array[3].add(addOrNegate(faceRadius, true, false));
        return array;
    }

    public static Vector3f addOrNegate(Tuple3f coord, boolean u, boolean v) {
        boolean zisv = coord.x != 0 && coord.y == 0;
        float x = coord.x * (u ? 1 : -1);
        float y = coord.y * (v ? -1 : 1);
        float z = coord.z * (zisv ? (v ? -1 : 1) : (u ? 1 : -1));
        Vector3f neg = new Vector3f(x, y, z);
        return neg;
    }

    public static boolean shouldInvertForRender(EnumFacing face) {
        boolean flip = face.getAxisDirection() == AxisDirection.NEGATIVE;
        if (face.getAxis() == Axis.Z) flip = !flip;
        return flip;
    }

    public static EnumFacing faceForRender(EnumFacing face) {
        if (shouldInvertForRender(face)) return face.getOpposite();
        return face;
    }
}
