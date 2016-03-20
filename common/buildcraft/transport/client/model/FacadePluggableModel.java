package buildcraft.transport.client.model;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.utils.MatrixUtils;

import javax.vecmath.Matrix4f;

public final class FacadePluggableModel extends BakedModelHolder implements IPluggableModelBaker<ModelKeyFacade> {
    private static final ResourceLocation hollowLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_hollow.obj");
    private static final ResourceLocation filledLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_filled.obj");
    public static final FacadePluggableModel INSTANCE = new FacadePluggableModel();

    private FacadePluggableModel() {
        // We only extend BakedModelHolder to get the model functions
    }

    public IModel modelHollow() {
        return getModelOBJ(hollowLoc);
    }

    public IModel modelFilled() {
        return getModelOBJ(filledLoc);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    @Override
    public ImmutableList<BakedQuad> bake(ModelKeyFacade key) {
        return ImmutableList.copyOf(bakeCutout(key.side, key.hollow, key.state, getVertexFormat()));
    }

    public List<BakedQuad> bakeCutout(EnumFacing face, boolean hollow, IBlockState state, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        // FIXME: Use the model bisector to cut a model down + squish one side down so it looks right
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

        IModel model;
        if (hollow) {
            model = modelHollow();
        } else {
            model = modelFilled();
        }

        if (model != null) {
            IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
            for (BakedQuad quad : baked.getGeneralQuads()) {
                MutableQuad mutable = MutableQuad.create(quad);
                mutable.transform(matrix);
                mutable.setCalculatedDiffuse();
                BCModelHelper.appendBakeQuads(quads, mutable);
            }
        }
        return quads;
    }
}
