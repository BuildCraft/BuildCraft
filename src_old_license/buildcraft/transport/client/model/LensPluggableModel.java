package buildcraft.transport.client.model;

import java.util.Collections;
import java.util.List;
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

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.PerspAwareModelBase;
import buildcraft.core.lib.client.sprite.SubSprite;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.pluggable.ItemLens;
import buildcraft.transport.pluggable.LensPluggable;

import javax.vecmath.Matrix4f;

public final class LensPluggableModel extends BakedModelHolder implements IPluggableModelBaker<ModelKeyLens> {
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
        quads.addAll(INSTANCE.bakeCutout(lens.isFilter, EnumFacing.EAST, format));
        quads.addAll(INSTANCE.bakeTransclucent(lens.dyeColor, lens.isFilter, EnumFacing.EAST, format));
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
        spriteWaterFlow = new SubSprite(spriteWaterFlow, 8, 8);
        // The water sprite flows upwards if we don't flip the V
        spriteWaterFlow = new SubSprite.FlippedV(spriteWaterFlow);
    }

    @Override
    public ImmutableList<BakedQuad> bake(ModelKeyLens key) {
        if (key instanceof ModelKeyLens.Cutout) {
            return bakeCutout((ModelKeyLens.Cutout) key);
        } else if (key instanceof ModelKeyLens.Translucent) {
            return bakeTranslucent((ModelKeyLens.Translucent) key);
        } else if (key == null) {
            throw new NullPointerException("key");
        } else {
            throw new IllegalArgumentException("Invalid key type " + key.getClass());
        }
    }

    private ImmutableList<BakedQuad> bakeCutout(ModelKeyLens.Cutout key) {
        return ImmutableList.copyOf(bakeCutout(key.isFilter, key.side, getVertexFormat()));
    }

    private ImmutableList<BakedQuad> bakeTranslucent(ModelKeyLens.Translucent key) {
        return ImmutableList.copyOf(bakeTransclucent(key.colour, key.isFilter, key.side, getVertexFormat()));
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    private List<BakedQuad> bakeCutout(boolean isFilter, EnumFacing face, VertexFormat format) {
        IModel model = isFilter ? modelCutoutFilter() : modelCutoutLens();
        TextureAtlasSprite sprite = isFilter ? spriteFilterCutout : spriteLensCutout;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderLens(model, sprite, format);
        Matrix4f matrix = MatrixUtil.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.transform(matrix);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    private List<BakedQuad> bakeTransclucent(EnumDyeColor colour, boolean isFilter, EnumFacing face, VertexFormat format) {
        TextureAtlasSprite sprite = spriteTranslucent;
        int shade = -1;
        if (colour == null) {
            if (isFilter) return Collections.emptyList();
            sprite = spriteWaterFlow;
        } else {
            shade = ColorUtils.getLightHex(colour);
        }

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderLens(modelTranslucent(), sprite, format);
        Matrix4f matrix = MatrixUtil.rotateTowardsFace(face);
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
