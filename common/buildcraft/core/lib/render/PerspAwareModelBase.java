package buildcraft.core.lib.render;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.TRSRTransformation;

public class PerspAwareModelBase implements IPerspectiveAwareModel, IFlexibleBakedModel {
    private final VertexFormat format;
    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<TransformType, TRSRTransformation> transforms;

    public PerspAwareModelBase(VertexFormat format, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle,
            ImmutableMap<TransformType, TRSRTransformation> transforms) {
        this.format = format;
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
    }

    @Override
    public VertexFormat getFormat() {
        return format;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particle;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }
}
