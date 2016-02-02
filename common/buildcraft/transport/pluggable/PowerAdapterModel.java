package buildcraft.transport.pluggable;

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
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.render.PerspAwareModelBase;
import buildcraft.core.lib.utils.MatrixUtils;

public class PowerAdapterModel extends BakedModelHolder implements IPipePluggableStaticRenderer {
    public static final PowerAdapterModel INSTANCE = new PowerAdapterModel();

    private static final ResourceLocation powerAdapterLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/power_adapter.obj");

    private static final ResourceLocation powerAdapterSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/power_adapter");
    private static TextureAtlasSprite spritePowerAdapter;

    private PowerAdapterModel() {}

    public static PerspAwareModelBase create() {
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        VertexFormat format = DefaultVertexFormats.ITEM;
        quads.addAll(INSTANCE.bakeCutout(EnumFacing.EAST, format));
        return new PerspAwareModelBase(format, quads.build(), spritePowerAdapter, getBlockTransforms());
    }

    public IModel modelPowerAdapter() {
        return getModelOBJ(powerAdapterLoc);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre event) {
        spritePowerAdapter = null;
        spritePowerAdapter = event.map.getTextureExtry(powerAdapterSpriteLoc.toString());
        if (spritePowerAdapter == null) spritePowerAdapter = event.map.registerSprite(powerAdapterSpriteLoc);
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        return bakeCutout(face, DefaultVertexFormats.BLOCK);
    }

    private List<BakedQuad> bakeCutout(EnumFacing face, VertexFormat format) {
        IModel model = modelPowerAdapter();
        TextureAtlasSprite sprite = spritePowerAdapter;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderAdapter(model, sprite, format);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            quad = transform(quad, matrix);
            // quad = applyDiffuse(quad);
            quads.add(quad);
        }

        return quads;
    }

    public static List<BakedQuad> renderAdapter(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
        List<BakedQuad> quads = Lists.newArrayList();
        IFlexibleBakedModel baked = model.bake(ModelRotation.X0_Y0, format, singleTextureFunction(sprite));
        for (BakedQuad quad : baked.getGeneralQuads()) {
            quad = replaceShade(quad, 0xFFFFFFFF);
            quads.add(quad);
        }
        return quads;
    }
}
