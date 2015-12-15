package buildcraft.transport.render;

import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.transport.gates.GatePluggable;

public final class GatePluggableRenderer extends BakedModelHolder implements IPipePluggableStaticRenderer, IPipePluggableDynamicRenderer {
    private static final ResourceLocation mainLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_main.obj");
    private static final ResourceLocation materialLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/gate_material.obj");
    public static final GatePluggableRenderer INSTANCE = new GatePluggableRenderer();

    private GatePluggableRenderer() {}

    private IModel modelMain() {
        return getModelOBJ(mainLoc);
    }

    private IModel modelMaterial() {
        return getModelOBJ(materialLoc);
    }

    @Override
    public void renderDynamicPluggable(IPipe pipe, EnumFacing side, PipePluggable pipePluggable, double x, double y, double z) {

    }

    @Override
    public List<BakedQuad> renderStaticPluggable(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        GatePluggable gate = (GatePluggable) pluggable;

        final TextureAtlasSprite gateSprite = gate.isLit ? gate.logic.getIconLit() : gate.logic.getIconDark();
        final TextureAtlasSprite matSprite = gate.material.getIconBlock();

        IModel main = modelMain();
        IModel material = modelMaterial();

        List<BakedQuad> quads = Lists.newArrayList();
        if (main != null && material != null) {
            Matrix4f matrix = rotateTowardsFace(face);

            IFlexibleBakedModel baked = main.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK,
                    new Function<ResourceLocation, TextureAtlasSprite>() {
                        @Override
                        public TextureAtlasSprite apply(ResourceLocation input) {
                            return gateSprite;
                        }
                    });
            for (BakedQuad quad : baked.getGeneralQuads()) {
                quad = transform(quad, matrix);
                quad = replaceShade(quad, 0xFFFFFFFF);
                quad = applyDiffuse(quad);
                quads.add(quad);
            }

            if (matSprite != null) {
                baked = material.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, new Function<ResourceLocation, TextureAtlasSprite>() {
                    @Override
                    public TextureAtlasSprite apply(ResourceLocation input) {
                        return matSprite;
                    }
                });
                for (BakedQuad quad : baked.getGeneralQuads()) {
                    quad = transform(quad, matrix);
                    quad = replaceShade(quad, 0xFFFFFFFF);
                    quad = applyDiffuse(quad);
                    quads.add(quad);
                }
            }
        }

        return quads;
    }
}
