package buildcraft.core.lib.render;

import java.util.List;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.client.model.ItemLayerModel.BakedModel;
import net.minecraftforge.client.model.TRSRTransformation;

public abstract class BuildCraftBakedModel extends BakedModel {
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
    public static final int UNUSED = 6;

    // Size of each array
    public static final int ARRAY_SIZE = 7;

    @SuppressWarnings("deprecation")
    public BuildCraftBakedModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
            ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(quads, particle, format, transforms);
    }

    public BuildCraftBakedModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        this(quads, particle, format, getBlockTransforms());
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for inside inventories and third person */
    protected static ImmutableMap<TransformType, TRSRTransformation> getBlockTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();

        float scale = 0.375f;
        Vector3f translation = new Vector3f(0, 1.5F * scale, -2.75F * scale);
        TRSRTransformation trsr = new TRSRTransformation(translation, new Quat4f(10, -45, 170, 1), new Vector3f(0.375F, 0.375F, 0.375F), null);
        builder.put(TransformType.THIRD_PERSON, trsr);

        // translation = new Vector3f(1, 1, 0);
        // trsr = new TRSRTransformation(translation, new Quat4f(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quat4f(0, -90,
        // 90, 1));
        // builder.put(TransformType.GUI, trsr);

        return builder.build();
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for inside inventories and third person */
    protected static ImmutableMap<TransformType, TRSRTransformation> getItemTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();

        float scale = 0.375f;
        Vector3f translation = new Vector3f(0, 1.5F * scale, -2.75F * scale);
        TRSRTransformation trsr = new TRSRTransformation(translation, new Quat4f(10, -45, 170, 1), new Vector3f(0.375F, 0.375F, 0.375F), null);
        builder.put(TransformType.THIRD_PERSON, trsr);

        translation = new Vector3f(1, 1, 0);
        trsr = new TRSRTransformation(translation, new Quat4f(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quat4f(0, -90, 90, 1));
        builder.put(TransformType.GUI, trsr);

        return builder.build();
    }

    /** Returns an array of suitable arrays for baked quads of two sides. Use
     * {@link #bakeQuads(List, int[][], EnumFacing[])} to add them to the array. */
    public static int[][] getDoubleFrom(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float[] uvs) {
        int[][] arr = new int[2][];
        arr[0] = getFrom(p1, p2, p3, p4, uvs);

        float[] duvs = new float[4];
        duvs[U_MIN] = uvs[U_MAX];
        duvs[U_MAX] = uvs[U_MIN];
        duvs[V_MIN] = uvs[V_MIN];
        duvs[V_MAX] = uvs[V_MAX];
        arr[1] = getFrom(p4, p3, p2, p1, duvs);
        return arr;
    }

    /** Like {@link #getDoubleFrom(Vector3f, Vector3f, Vector3f, Vector3f, float[])}, but takes a size 4 vector array
     * instead */
    public static int[][] getDoubleFrom(Vector3f[] array, float[] uvs) {
        return getDoubleFrom(array[0], array[1], array[2], array[3], uvs);
    }

    public static int[] getFrom(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float[] uvs) {
        int[] i1 = new int[] { asInt(p1.x), asInt(p1.y), asInt(p1.z), -1, asInt(uvs[U_MIN]), asInt(uvs[V_MIN]), 0 };
        int[] i2 = new int[] { asInt(p2.x), asInt(p2.y), asInt(p2.z), -1, asInt(uvs[U_MIN]), asInt(uvs[V_MAX]), 0 };
        int[] i3 = new int[] { asInt(p3.x), asInt(p3.y), asInt(p3.z), -1, asInt(uvs[U_MAX]), asInt(uvs[V_MAX]), 0 };
        int[] i4 = new int[] { asInt(p4.x), asInt(p4.y), asInt(p4.z), -1, asInt(uvs[U_MAX]), asInt(uvs[V_MIN]), 0 };
        return concat(i1, i2, i3, i4);
    }

    public static float[] getUVArray(TextureAtlasSprite sprite) {
        float[] uvs = new float[4];
        uvs[U_MIN] = sprite.getMinU();
        uvs[U_MAX] = sprite.getMaxU();
        uvs[V_MIN] = sprite.getMinV();
        uvs[V_MAX] = sprite.getMaxV();
        return uvs;
    }

    public static int[] getFrom(Vector3f[] array, float[] uvs) {
        return getFrom(array[0], array[1], array[2], array[3], uvs);
    }

    public static int asInt(float f) {
        return Float.floatToRawIntBits(f);
    }

    public static int[] concat(int[]... ints) {
        int[] holder = ints[0];
        for (int i = 1; i < ints.length; i++) {
            holder = ArrayUtils.addAll(holder, ints[i]);
        }
        return holder;
    }

    public static void bakeQuad(List<BakedQuad> quads, int[] list, EnumFacing side) {
        quads.add(new BakedQuad(list, 0, side));
    }

    public static void bakeQuads(List<BakedQuad> quads, int[][] lists, EnumFacing... sides) {
        for (int i = 0; i < lists.length; i++) {
            bakeQuad(quads, lists[i], sides[i]);
        }
    }

    public static Vector3f[] getPoints(Vector3f centerFace, Vector3f faceRadius) {
        Vector3f[] array = new Vector3f[4];

        array[0] = new Vector3f(centerFace);
        array[1] = new Vector3f(centerFace);
        array[2] = new Vector3f(centerFace);
        array[3] = new Vector3f(centerFace);

        array[0].add(addOrNegate(faceRadius, false, false));
        array[1].add(addOrNegate(faceRadius, false, true));
        array[2].add(addOrNegate(faceRadius, true, true));
        array[3].add(addOrNegate(faceRadius, true, false));
        return array;
    }

    public static Vector3f addOrNegate(Vector3f coord, boolean u, boolean v) {
        boolean zisv = coord.x != 0 && coord.y == 0;
        Vector3f neg = new Vector3f(coord.x * (u ? 1 : -1), coord.y * (v ? -1 : 1), coord.z * (zisv ? (v ? -1 : 1) : (u ? 1 : -1)));
        return neg;
    }

    public static void bakeFace(List<BakedQuad> quads, EnumFacing face, Vector3f center, Vector3f radius, float[] uvs) {
        Vector3f[] points = getPointsForFace(face, center, radius);
        int[] quadData = getFrom(points, uvs);
        bakeQuad(quads, quadData, face);
    }

    public static void bakeDoubleFace(List<BakedQuad> quads, EnumFacing face, Vector3f center, Vector3f radius, float[] uvs) {
        Vector3f[] points = getPointsForFace(face, center, radius);
        int[][] quadData = getDoubleFrom(points, uvs);
        bakeQuads(quads, quadData, face.getOpposite(), face);
    }

    public static Vector3f[] getPointsForFace(EnumFacing face, Vector3f center, Vector3f radius) {
        Vector3f centerOfFace = new Vector3f(center);
        Vector3f faceAdd = new Vector3f(face.getFrontOffsetX() * radius.x, face.getFrontOffsetY() * radius.y, face.getFrontOffsetZ() * radius.z);
        centerOfFace.add(faceAdd);
        Vector3f faceRadius = new Vector3f(radius);
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            faceRadius.sub(faceAdd);
        } else {
            faceRadius.add(faceAdd);
        }
        return getPoints(centerOfFace, faceRadius);
    }
}
