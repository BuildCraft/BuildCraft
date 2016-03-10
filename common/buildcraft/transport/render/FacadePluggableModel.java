package buildcraft.transport.render;

import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.*;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.utils.MatrixUtils;

public final class FacadePluggableModel extends BakedModelHolder implements IPipePluggableStaticRenderer {
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
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFacadePluggable facade = (IFacadePluggable) pluggable;

        // FIXME: Use the model bisector to cut a model down + squish one side down so it looks right

        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(facade
                .getCurrentState());

        IModel model;
        if (facade.isHollow()) {
            model = modelHollow();
        } else {
            model = modelFilled();
        }

        if (model != null) {
            IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, singleTextureFunction(sprite));
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
            for (BakedQuad quad : baked.getGeneralQuads()) {
                quad = transform(quad, matrix);
                quad = replaceShade(quad, 0xFFFFFFFF);
                quad = applyDiffuse(quad);
                quads.add(quad);
            }
        }
        return quads;
    }
}
