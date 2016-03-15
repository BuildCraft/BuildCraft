package buildcraft.core.lib.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public class BCModelHelper {
    public static final int U_MIN = 0;
    public static final int U_MAX = 1;
    public static final int V_MIN = 2;
    public static final int V_MAX = 3;

    // Baked Quad array indices
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int SHADE = 3;
    public static final int U = 4;
    public static final int V = 5;
    /** Represents either the normal (for items) or lightmap (for blocks) */
    public static final int UNUSED = 6;

    // Size of each array
    public static final int ARRAY_SIZE = 7;

    public static MutableQuad createFace(EnumFacing face, Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d, float[] uvs) {
        MutableQuad mutable = new MutableQuad(-1, face);
        if (face == null || shouldInvertForRender(face)) {
            mutable.getVertex(0).positionv(a).texf(uvs[U_MIN], uvs[V_MIN]);
            mutable.getVertex(1).positionv(b).texf(uvs[U_MIN], uvs[V_MAX]);
            mutable.getVertex(2).positionv(c).texf(uvs[U_MAX], uvs[V_MAX]);
            mutable.getVertex(3).positionv(d).texf(uvs[U_MAX], uvs[V_MIN]);
        } else {
            mutable.getVertex(3).positionv(a).texf(uvs[U_MIN], uvs[V_MIN]);
            mutable.getVertex(2).positionv(b).texf(uvs[U_MIN], uvs[V_MAX]);
            mutable.getVertex(1).positionv(c).texf(uvs[U_MAX], uvs[V_MAX]);
            mutable.getVertex(0).positionv(d).texf(uvs[U_MAX], uvs[V_MIN]);
        }
        return mutable;
    }

    public static <T extends Tuple3f> MutableQuad createFace(EnumFacing face, T[] points, float[] uvs) {
        return createFace(face, points[0], points[1], points[2], points[3], uvs);
    }

    public static MutableQuad createFace(EnumFacing face, Tuple3f center, Tuple3f radius, float[] uvs) {
        Point3f[] points = BCModelHelper.getPointsForFace(face, center, radius);
        return createFace(face, points, uvs);
    }

    public static MutableQuad createInverseFace(EnumFacing face, Tuple3f center, Tuple3f radius, float[] uvs) {
        return createFace(face, center, radius, uvs).invertNormal();
    }

    public static MutableQuad[] createDoubleFace(EnumFacing face, Tuple3f center, Tuple3f radius, float[] uvs) {
        MutableQuad norm = createFace(face, center, radius, uvs);
        return new MutableQuad[] { norm, norm.deepClone().invertNormal() };
    }

    public static List<MutableQuad> toMutableQuadList(IBakedModel model, boolean includeFaces) {
        List<MutableQuad> quads = new ArrayList<>();
        for (BakedQuad q : model.getGeneralQuads()) {
            quads.add(MutableQuad.create(q));
        }
        if (includeFaces) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                for (BakedQuad q : model.getFaceQuads(facing)) {
                    quads.add(MutableQuad.create(q));
                }
            }
        }
        return quads;
    }

    public static void appendQuads(List<MutableQuad> to, MutableQuad... from) {
        for (MutableQuad q : from) {
            to.add(q);
        }
    }

    public static void appendBakeQuads(List<BakedQuad> to, MutableQuad... from) {
        for (MutableQuad q : from) {
            to.add(q.toUnpacked());
        }
    }

    public static void appendBakeQuads(List<BakedQuad> to, VertexFormat format, MutableQuad... from) {
        for (MutableQuad q : from) {
            to.add(q.toUnpacked(format));
        }
    }

    public static void appendBakeQuads(List<BakedQuad> to, Collection<MutableQuad> from) {
        for (MutableQuad q : from) {
            to.add(q.toUnpacked());
        }
    }

    public static List<BakedQuad> bakeList(List<MutableQuad> from) {
        List<BakedQuad> to = new ArrayList<>();
        appendBakeQuads(to, from);
        return to;
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
