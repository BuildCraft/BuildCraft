package buildcraft.robotics.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.BuildCraftCore;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableState;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipeRenderState;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.*;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.robotics.RobotStationPluggable.EnumRobotStationState;

public class RobotStationModel extends BakedModelHolder implements IPipePluggableStaticRenderer {
    public static final RobotStationModel INSTANCE = new RobotStationModel();

    private static final ResourceLocation baseLoc = new ResourceLocation("buildcraftrobotics:models/pluggables/robot_station_base.obj");

    private TextureAtlasSprite baseSprite;
    private final Map<EnumRobotStationState, TextureAtlasSprite> stateSprites = Maps.newEnumMap(EnumRobotStationState.class);
    private final Map<EnumRobotStationState, List<MutableQuad>> stateQuads = Maps.newEnumMap(EnumRobotStationState.class);
    private final List<MutableQuad> modelBaseQuads = new ArrayList<>();

    private IModel modelBase() {
        return getModelOBJ(baseLoc);
    }

    public PerspAwareModelBase createItemModel() {
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        VertexFormat format = DefaultVertexFormats.ITEM;
        quads.addAll(INSTANCE.bakeCutout(EnumRobotStationState.Available, EnumFacing.SOUTH, format));
        return new PerspAwareModelBase(format, quads.build(), baseSprite, getPluggableTransforms());
    }

    @SubscribeEvent
    public void textureStitchPre(TextureStitchEvent.Pre pre) {
        TextureMap map = pre.map;
        baseSprite = null;
        baseSprite = map.getTextureExtry("buildcraftrobotics:station/base");
        if (baseSprite == null) baseSprite = map.registerSprite(new ResourceLocation("buildcraftrobotics:station/base"));

        for (final EnumRobotStationState state : EnumRobotStationState.values()) {
            String suffix = state.getTextureSuffix() + (BuildCraftCore.colorBlindMode ? "_cb" : "");
            ResourceLocation location = new ResourceLocation("buildcraftrobotics:station/state_" + suffix);
            TextureAtlasSprite sprite = map.getTextureExtry(location.toString());
            if (sprite == null) sprite = map.registerSprite(location);
            stateSprites.put(state, sprite);
        }
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post post) {
        for (final EnumRobotStationState state : EnumRobotStationState.values()) {
            Matrix4f translation = new Matrix4f();
            translation.setIdentity();
            translation.setTranslation(new Vector3f(2.8f / 16f, 0, 0));

            List<MutableQuad> quads = Lists.newArrayList();
            for (MutableQuad mutable : BuildCraftBakedModel.createQuadsItemLayer(stateSprites.get(state))) {
                mutable.transform(translation);
                mutable.colouri(0xFF_FF_FF_FF);
                quads.add(mutable);
            }
            stateQuads.put(state, quads);
        }
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        RobotStationPluggable station = (RobotStationPluggable) pluggable;

        return bakeCutout(station.getRenderState(), face, DefaultVertexFormats.BLOCK);
    }

    private List<MutableQuad> baseQuads() {
        if (modelBaseQuads.isEmpty()) {
            IModel base = modelBase();
            if (base != null) {
                IFlexibleBakedModel baked = base.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, BuildCraftBakedModel.singleTextureFunction(
                        baseSprite));
                for (BakedQuad quad : baked.getGeneralQuads()) {
                    MutableQuad mutable = MutableQuad.create(quad);
                    modelBaseQuads.add(mutable);
                }
            }
        }
        return modelBaseQuads;
    }

    private List<BakedQuad> bakeCutout(EnumRobotStationState state, EnumFacing face, VertexFormat format) {
        final TextureAtlasSprite baseSprite = this.baseSprite;

        IModel base = modelBase();
        List<MutableQuad> stateQuads = this.stateQuads.get(state);

        List<BakedQuad> quads = Lists.newArrayList();
        if (base != null) {
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);

            for (MutableQuad mutable : baseQuads()) {
                mutable = mutable.deepClone();
                mutable.transform(matrix);
                mutable.setCalculatedDiffuse();
                BCModelHelper.appendBakeQuads(quads, format, mutable);
            }

            if (stateQuads != null) {
                for (MutableQuad mutable : stateQuads) {
                    mutable = mutable.deepClone();
                    mutable.transform(matrix);
                    mutable.setCalculatedDiffuse();
                    BCModelHelper.appendBakeQuads(quads, format, mutable);
                }
            }
        }
        return quads;
    }
}
