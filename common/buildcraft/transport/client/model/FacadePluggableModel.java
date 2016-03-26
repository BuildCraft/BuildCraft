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
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;

import javax.vecmath.Matrix4f;

public final class FacadePluggableModel extends BakedModelHolder implements IPluggableModelBaker<ModelKeyFacade> {
    private static final ResourceLocation hollowLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_hollow.obj");
    private static final ResourceLocation filledLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_filled.obj");
    private static final ResourceLocation connectorLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/plug.obj");
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

    public IModel modelConnector() {
        return getModelOBJ(connectorLoc);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    @Override
    public ImmutableList<BakedQuad> bake(ModelKeyFacade key) {
        return ImmutableList.copyOf(bake(key.layer, key.side, key.hollow, key.state, getVertexFormat()));
    }

    public List<BakedQuad> bake(EnumWorldBlockLayer layer, EnumFacing face, boolean hollow, IBlockState state, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            if (!state.getBlock().canRenderInLayer(EnumWorldBlockLayer.TRANSLUCENT)) {
                return quads;
            }
        } else {
            if (!state.getBlock().canRenderInLayer(EnumWorldBlockLayer.SOLID)
                    && !state.getBlock().canRenderInLayer(EnumWorldBlockLayer.CUTOUT)
                    && !state.getBlock().canRenderInLayer(EnumWorldBlockLayer.CUTOUT_MIPPED)) {
                return quads;
            }
        }

        // FIXME: Use the model bisector to cut a model down + squish one side down so it looks right
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);

        IModel model;
        if (hollow) {
            model = modelHollow();
        } else {
            model = modelFilled();
        }

        if (model != null) {
            IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
            for (BakedQuad quad : baked.getGeneralQuads()) {
                MutableQuad mutable = MutableQuad.create(quad);
                mutable.transform(matrix);
                mutable.setCalculatedDiffuse();
                BCModelHelper.appendBakeQuads(quads, mutable);
            }
        }

        if (!hollow && !ItemFacade.isTransparentFacade(state)) {
            IModel connector = modelConnector();
            TextureAtlasSprite structure = PipeIconProvider.TYPE.PipeStructureCobblestone.getIcon();
            IFlexibleBakedModel baked = connector.bake(ModelRotation.X0_Y0, format, singleTextureFunction(structure));
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
