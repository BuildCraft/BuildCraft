package buildcraft.robotics.render;

import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import buildcraft.core.lib.render.BakedModelHolder;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.robotics.RobotStationPluggable;
import buildcraft.robotics.RobotStationPluggable.EnumRobotStationState;

public class RobotStationRenderer extends BakedModelHolder implements IPipePluggableStaticRenderer {
    public static final RobotStationRenderer INSTANCE = new RobotStationRenderer();

    private static final ResourceLocation baseLoc = new ResourceLocation("buildcraftrobotics:models/pluggables/robot_station_base.obj");

    private TextureAtlasSprite baseSprite;
    private final Map<EnumRobotStationState, TextureAtlasSprite> stateSprites = Maps.newEnumMap(EnumRobotStationState.class);
    private final Map<EnumRobotStationState, List<BakedQuad>> stateQuads = Maps.newEnumMap(EnumRobotStationState.class);

    private IModel modelBase() {
        return getModelOBJ(baseLoc);
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

            List<BakedQuad> quads = Lists.newArrayList();
            for (BakedQuad quad : BuildCraftBakedModel.createModelItemLayer(stateSprites.get(state)).getGeneralQuads()) {
                quad = transform(quad, translation);
                quad = replaceTint(quad, 0xFFFFFF);
                quads.add(quad);
            }
            stateQuads.put(state, quads);
        }
    }

    @Override
    public List<BakedQuad> bakeCutout(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face) {
        RobotStationPluggable station = (RobotStationPluggable) pluggable;

        final TextureAtlasSprite baseSprite = this.baseSprite;

        IModel base = modelBase();
        List<BakedQuad> stateQuads = this.stateQuads.get(station.getRenderState());

        List<BakedQuad> quads = Lists.newArrayList();
        if (base != null) {
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(face);

            IFlexibleBakedModel baked = base.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK,
                    new Function<ResourceLocation, TextureAtlasSprite>() {
                        @Override
                        public TextureAtlasSprite apply(ResourceLocation input) {
                            return baseSprite;
                        }
                    });
            for (BakedQuad quad : baked.getGeneralQuads()) {
                quad = transform(quad, matrix);
                quad = replaceShade(quad, 0xFFFFFFFF);
                quad = applyDiffuse(quad);
                quads.add(quad);
            }

            if (stateQuads != null) {
                for (BakedQuad quad : stateQuads) {
                    quad = transform(quad, matrix);
                    quad = applyDiffuse(quad);
                    quads.add(quad);
                }
            }
        }

        return quads;
    }
}
