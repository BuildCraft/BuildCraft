package buildcraft.transport.render;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.transport.ItemPipe;

public class PipeItemModel extends BuildCraftBakedModel {
    protected PipeItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle) {
        super(quads, particle, DefaultVertexFormats.BLOCK, getItemTransforms());
    }

    public static PipeItemModel create(ItemPipe item) {
        List<BakedQuad> quads = Lists.newArrayList();
        TextureAtlasSprite sprite = item.getSprite();

        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.5f, 0.25f);

        for (EnumFacing face : EnumFacing.VALUES) {
            boolean vertical = face.getAxis() == Axis.Y;
            float[] uvs = new float[4];
            uvs[U_MIN] = sprite.getInterpolatedU(4);
            uvs[U_MAX] = sprite.getInterpolatedU(12);
            uvs[V_MIN] = vertical ? sprite.getInterpolatedV(4) : sprite.getMinV();
            uvs[V_MAX] = vertical ? sprite.getInterpolatedV(12) : sprite.getMaxV();
            bakeFace(quads, face, center, radius, uvs);
        }

        return new PipeItemModel(ImmutableList.copyOf(quads), sprite);
    }
}
