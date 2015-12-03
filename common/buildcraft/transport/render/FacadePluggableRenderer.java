package buildcraft.transport.render;

import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.base.Function;
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
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.BakedModelHolder;

public final class FacadePluggableRenderer extends BakedModelHolder implements IPipePluggableStaticRenderer {
    private static final ResourceLocation hollowLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_hollow.obj");
    private static final ResourceLocation filledLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/facade_filled.obj");
    public static final IPipePluggableStaticRenderer INSTANCE = new FacadePluggableRenderer();

    private FacadePluggableRenderer() {
        // We only extend BuildCraftBakedModel to get the model functions
    }

    private IModel modelHollow() {
        return getModel(hollowLoc);
    }

    private IModel modelFilled() {
        return getModel(filledLoc);
    }

    @Override
    public List<BakedQuad> renderStaticPluggable(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFacadePluggable facade = (IFacadePluggable) pluggable;

        // Use the particle texture for the block. Not ideal, but we have NO way of getting the actual
        // texture of the block without hackery...
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(facade
                .getCurrentState());

        IModel model;
        if (facade.isHollow()) {
            model = modelHollow();
        } else {
            model = modelFilled();
        }

        if (model != null) {
            IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK,
                    new Function<ResourceLocation, TextureAtlasSprite>() {
                        @Override
                        public TextureAtlasSprite apply(ResourceLocation input) {
                            return sprite;
                        }
                    });
            Matrix4f matrix = rotateTowardsFace(face);
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
