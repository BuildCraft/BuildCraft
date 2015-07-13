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
        super(quads, particle, format);
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for inside inventories and third person */
    protected static ImmutableMap<TransformType, TRSRTransformation> getTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();

        // builder.put(TransformType.GUI, TRSRTransformation.identity());

        float scale = 0.0375f;
        Vector3f translation = new Vector3f(0, 1.5F * scale, -2.75F * scale);
        TRSRTransformation trsr = new TRSRTransformation(translation, null, new Vector3f(0.375F, 0.375F, 0.375F), new Quat4f(10, -45, 170, 1));
        builder.put(TransformType.THIRD_PERSON, trsr);

        return builder.build();
    }

    /** Returns an array of suitable arrays for baked quads of two sides. Use
     * {@link #bakeQuads(List, int[][], EnumFacing[])} to add them to the array. */
    public static int[][] getDoubleFrom(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float[] uvs) {
        int[][] arr = new int[2][];
        arr[0] = getFrom(p1, p2, p3, p4, uvs);
        arr[1] = getFrom(p4, p3, p2, p1, uvs);
        return arr;
    }

    public static int[] getFrom(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float[] uvs) {
        int[] i1 = new int[] { asInt(p1.x), asInt(p1.y), asInt(p1.z), -1, asInt(uvs[U_MIN]), asInt(uvs[V_MIN]), 0 };
        int[] i2 = new int[] { asInt(p2.x), asInt(p2.y), asInt(p2.z), -1, asInt(uvs[U_MIN]), asInt(uvs[V_MAX]), 0 };
        int[] i3 = new int[] { asInt(p3.x), asInt(p3.y), asInt(p3.z), -1, asInt(uvs[U_MAX]), asInt(uvs[V_MAX]), 0 };
        int[] i4 = new int[] { asInt(p4.x), asInt(p4.y), asInt(p4.z), -1, asInt(uvs[U_MAX]), asInt(uvs[V_MIN]), 0 };
        return concat(i1, i2, i3, i4);
    }

    private static int asInt(float f) {
        return Float.floatToRawIntBits(f);
    }

    private static int[] concat(int[]... ints) {
        int[] holder = ints[0];
        for (int i = 1; i < ints.length; i++) {
            holder = ArrayUtils.addAll(holder, ints[i]);
        }
        return holder;
    }

    public static void bakeQuad(List<BakedQuad> quads, int[] list, EnumFacing side) {
        quads.add(new BakedQuad(list, 0, side));
    }

    public static void bakeQuads(List<BakedQuad> quads, int[][] lists, EnumFacing[] sides) {
        for (int i = 0; i < lists.length; i++) {
            bakeQuad(quads, lists[i], sides[i]);
        }
    }
}
