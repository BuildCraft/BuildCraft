package buildcraft.transport.client.model;

import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.*;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.client.model.PerspAwareModelBase;
import buildcraft.core.lib.utils.MatrixUtils;

public class PlugPluggableModel extends BakedModelHolder implements IPluggableStaticBaker, PluggableModelBaker.Cutout<ModelKeyPlug> {
    public static final PlugPluggableModel INSTANCE = new PlugPluggableModel();

    private static final ResourceLocation plugLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/plug.obj");

    private static final ResourceLocation plugSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/plug");
    private static TextureAtlasSprite spritePlug;

    private PlugPluggableModel() {}

    public static PerspAwareModelBase create() {
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        VertexFormat format = DefaultVertexFormats.ITEM;
        quads.addAll(INSTANCE.bakeCutout(EnumFacing.SOUTH, format));
        return new PerspAwareModelBase(format, quads.build(), spritePlug, getPluggableTransforms());
    }

    public IModel modelPlug() {
        return getModelOBJ(plugLoc);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre event) {
        spritePlug = null;
        spritePlug = event.map.getTextureExtry(plugSpriteLoc.toString());
        if (spritePlug == null) spritePlug = event.map.registerSprite(plugSpriteLoc);
    }

    @Override
    public ImmutableList<BakedQuad> bakeCutout(ModelKeyPlug key) {
        return ImmutableList.copyOf(bakeCutout(key.side, getVertexFormat()));
    }

    @Override
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        return bakeCutout(face, DefaultVertexFormats.BLOCK);
    }

    private List<BakedQuad> bakeCutout(EnumFacing face, VertexFormat format) {
        IModel model = modelPlug();
        TextureAtlasSprite sprite = spritePlug;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderPlug(model, sprite, format);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.transform(matrix);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    public static List<BakedQuad> renderPlug(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
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
