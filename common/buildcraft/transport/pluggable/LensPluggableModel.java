package buildcraft.transport.pluggable;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
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
import buildcraft.core.lib.render.*;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.MatrixUtils;

public final class LensPluggableModel extends BakedModelHolder implements IPipePluggableStaticRenderer.Translucent {
    public static final LensPluggableModel INSTANCE = new LensPluggableModel();

    private static final ResourceLocation cutoutLensLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/lens_cutout.obj");
    private static final ResourceLocation cutoutFilterLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/filter_cutout.obj");
    private static final ResourceLocation translucentLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/lens_translucent.obj");

    private static final ResourceLocation cutoutLensSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/lens");
    private static final ResourceLocation cutoutFilterSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/filter");
    private static final ResourceLocation translucentSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/overlay_lens");
    private static TextureAtlasSprite spriteLensCutout, spriteFilterCutout, spriteTranslucent, spriteWaterFlow;

    private LensPluggableModel() {}

    public static PerspAwareModelBase create(ItemLens lensItem, int meta) {
        LensPluggable lens = new LensPluggable(new ItemStack(lensItem, 1, meta));
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        VertexFormat format = DefaultVertexFormats.ITEM;
        quads.addAll(INSTANCE.bakeCutout(lens, EnumFacing.EAST, format));
        quads.addAll(INSTANCE.bakeTransclucent(lens, EnumFacing.EAST, format));
        return new PerspAwareModelBase(format, quads.build(), spriteLensCutout, getBlockTransforms());
    }

    public IModel modelCutoutLens() {
        return getModelOBJ(cutoutLensLoc);
    }

    public IModel modelCutoutFilter() {
        return getModelOBJ(cutoutFilterLoc);
    }

    public IModel modelTranslucent() {
        return getModelOBJ(translucentLoc);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre event) {
        spriteLensCutout = null;
        spriteLensCutout = event.map.getTextureExtry(cutoutLensSpriteLoc.toString());
        if (spriteLensCutout == null) spriteLensCutout = event.map.registerSprite(cutoutLensSpriteLoc);

        spriteFilterCutout = null;
        spriteFilterCutout = event.map.getTextureExtry(cutoutFilterSpriteLoc.toString());
        if (spriteFilterCutout == null) spriteFilterCutout = event.map.registerSprite(cutoutFilterSpriteLoc);

        spriteTranslucent = null;
        spriteTranslucent = event.map.getTextureExtry(translucentSpriteLoc.toString());
        if (spriteTranslucent == null) spriteLensCutout = event.map.registerSprite(translucentSpriteLoc);
    }

    @SubscribeEvent
    public void textureGetter(TextureStitchEvent.Post event) {
        spriteWaterFlow = event.map.getAtlasSprite("minecraft:blocks/water_flow");
        // The water sprite is too big normally, so get 1/2 of each axis
        spriteWaterFlow = new SubIcon(spriteWaterFlow, 8, 8);
        // The water sprite flows upwards if we don't flip the V
        spriteWaterFlow = new SubIcon.FlippedV(spriteWaterFlow);
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        LensPluggable lens = (LensPluggable) pluggable;
        return bakeCutout(lens, face, DefaultVertexFormats.BLOCK);
    }

    @Override
    public List<BakedQuad> bakeTranslucent(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        LensPluggable lens = (LensPluggable) pluggable;
        return bakeTransclucent(lens, face, DefaultVertexFormats.BLOCK);
    }

    private List<BakedQuad> bakeCutout(LensPluggable lens, EnumFacing face, VertexFormat format) {
        IModel model = lens.isFilter ? modelCutoutFilter() : modelCutoutLens();
        TextureAtlasSprite sprite = lens.isFilter ? spriteFilterCutout : spriteLensCutout;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderLens(model, sprite, format);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.transform(matrix);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    private List<BakedQuad> bakeTransclucent(LensPluggable lens, EnumFacing face, VertexFormat format) {
        EnumDyeColor colour = lens.getColour();
        TextureAtlasSprite sprite = spriteTranslucent;
        int shade = -1;
        if (colour == null) {
            if (lens.isFilter) return Collections.emptyList();
            sprite = spriteWaterFlow;
        } else {
            shade = ColorUtils.getLightHex(colour);
        }

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderLens(modelTranslucent(), sprite, format);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.setTint(shade);
            mutable.transform(matrix);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    public static List<BakedQuad> renderLens(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.colouri(0xFF_FF_FF_FF);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }
        return quads;
    }
}
