package buildcraft.core.lib.render;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ItemLayerModel.BakedModel;

public abstract class BuildCraftBakedModel extends BakedModel {
    public static final int U_MIN = 0;
    public static final int U_MAX = 1;
    public static final int V_MIN = 2;
    public static final int V_MAX = 3;

    public BuildCraftBakedModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
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
}
