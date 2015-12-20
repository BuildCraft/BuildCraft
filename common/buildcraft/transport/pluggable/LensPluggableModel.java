package buildcraft.transport.pluggable;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.MatrixUtils;

public final class LensPluggableModel extends BakedModelHolder implements IPipePluggableStaticRenderer.Translucent {
    public static final LensPluggableModel INSTANCE = new LensPluggableModel();

    private static final ResourceLocation cutoutLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/lens_cutout.obj");
    private static final ResourceLocation translucentLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/lens_translucent.obj");
    private static final ResourceLocation cutoutSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/lens");
    private static final ResourceLocation translucentSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/overlay_lens");
    private static TextureAtlasSprite spriteCutout, spriteTranslucent;

    private LensPluggableModel() {}

    public IModel modelCutout() {
        return getModelOBJ(cutoutLoc);
    }

    public IModel modelTranslucent() {
        return getModelOBJ(translucentLoc);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre event) {
        spriteCutout = null;
        spriteCutout = event.map.getTextureExtry(cutoutSpriteLoc.toString());
        if (spriteCutout == null) spriteCutout = event.map.registerSprite(cutoutSpriteLoc);

        spriteTranslucent = null;
        spriteTranslucent = event.map.getTextureExtry(translucentSpriteLoc.toString());
        if (spriteTranslucent == null) spriteCutout = event.map.registerSprite(translucentSpriteLoc);
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        LensPluggable lens = (LensPluggable) pluggable;

        EnumDyeColor colour = lens.getColour();
        int shade = ColorUtils.getLightHex(colour);

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderLens(modelCutout(), spriteCutout, DefaultVertexFormats.BLOCK);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            quad = transform(quad, matrix);
            quad = replaceShade(quad, shade);
            quad = applyDiffuse(quad);
            quads.add(quad);
        }

        return quads;
    }

    public List<BakedQuad> renderLens(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            quad = replaceShade(quad, 0xFFFFFF);
            quads.add(quad);
        }
        return quads;
    }

    @Override
    public List<BakedQuad> bakeTranslucent(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        return Collections.emptyList();
    }
}
