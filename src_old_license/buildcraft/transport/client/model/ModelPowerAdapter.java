package buildcraft.transport.client.model;

import java.util.List;
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

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BakedModelHolder;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.client.model.PerspAwareModelBase;
import buildcraft.core.lib.utils.MatrixUtils;

import javax.vecmath.Matrix4f;

public class ModelPowerAdapter extends BakedModelHolder implements IPluggableModelBaker<ModelKeyPowerAdapter> {
    public static final ModelPowerAdapter INSTANCE = new ModelPowerAdapter();

    private static final ResourceLocation powerAdapterLoc = new ResourceLocation("buildcrafttransport:models/blocks/pluggables/power_adapter.obj");

    private static final ResourceLocation powerAdapterSpriteLoc = new ResourceLocation("buildcrafttransport:pipes/power_adapter");
    private static TextureAtlasSprite spritePowerAdapter;

    private ModelPowerAdapter() {}

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
    public VertexFormat getVertexFormat() {
        return DefaultVertexFormats.BLOCK;
    }

    @Override
    public ImmutableList<BakedQuad> bake(ModelKeyPowerAdapter key) {
        return ImmutableList.copyOf(bakeCutout(key.side, DefaultVertexFormats.BLOCK));
    }

    private List<BakedQuad> bakeCutout(EnumFacing face, VertexFormat format) {
        IModel model = modelPowerAdapter();
        TextureAtlasSprite sprite = spritePowerAdapter;

        List<BakedQuad> quads = Lists.newArrayList();
        List<BakedQuad> bakedQuads = renderAdapter(model, sprite, format);
        Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);
        for (BakedQuad quad : bakedQuads) {
            MutableQuad mutable = MutableQuad.create(quad);
            mutable.transform(matrix);
            BCModelHelper.appendBakeQuads(quads, format, mutable);
        }

        return quads;
    }

    public static List<BakedQuad> renderAdapter(IModel model, TextureAtlasSprite sprite, VertexFormat format) {
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
