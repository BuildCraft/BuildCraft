package buildcraft.transport.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Tuple3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer.Translucent;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.config.DetailedConfigOption;
import buildcraft.core.lib.render.BCModelHelper;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.render.MutableQuad;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.*;
import buildcraft.transport.TileGenericPipe.CoreState;
import buildcraft.transport.render.tile.PipeRendererWires;

public class PipeBlockModel extends BuildCraftBakedModel implements ISmartBlockModel {
    private static final DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.67");

    public PipeBlockModel() {
        super(ImmutableList.<BakedQuad> of(), null, null);
    }

    protected PipeBlockModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
    }

    @Override
    public ISmartBlockModel handleBlockState(IBlockState state) {
        try {
            return handle((IExtendedBlockState) state);
        } catch (Throwable t) {
            t.printStackTrace();
            return defaultModel();
        }
    }

    private static ISmartBlockModel defaultModel() {
        return new PipeBlockModel();
    }

    public static ISmartBlockModel handle(IExtendedBlockState state) {
        CoreState core = BlockGenericPipe.PIPE_CORE_STATE.getUnlistedValue(state);// Not required... :P
        PipeRenderState render = BlockGenericPipe.PIPE_RENDER_STATE.getUnlistedValue(state);
        PipePluggableState pluggable = BlockGenericPipe.PIPE_PLUGGABLE_STATE.getUnlistedValue(state);
        Pipe<?> pipe = BlockGenericPipe.PIPE_PIPE.getUnlistedValue(state);

        if (core == null || render == null || pluggable == null || pipe == null) {
            return defaultModel();// Thats not good. Just return a cobblestone structure pipe centre model
        }

        List<BakedQuad> quads = Lists.newArrayList();

        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            renderCutoutPass(render, pluggable, pipe, quads);
        } else if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            renderTranslucentPass(render, pluggable, pipe, quads);
        }

        TextureAtlasSprite particle = pipe.getIconProvider().getIcon(pipe.getIconIndex(null));

        return new PipeBlockModel(ImmutableList.copyOf(quads), particle, DefaultVertexFormats.BLOCK);
    }

    private static void renderPipe(PipeRenderState render, List<BakedQuad> quads, Map<EnumFacing, TextureAtlasSprite> spriteMap, boolean smaller) {
        float min = CoreConstants.PIPE_MIN_POS;
        float max = CoreConstants.PIPE_MAX_POS;

        float minUV = min * 16;
        float maxUV = max * 16;

        float insideColourMult = OPTION_INSIDE_COLOUR_MULT.getAsFloatCapped(0, 1);

        // Center bit
        {
            TextureAtlasSprite sprite = spriteMap.get(null);

            float[] uvs = new float[4];
            uvs[U_MIN] = sprite.getInterpolatedU(minUV);
            uvs[U_MAX] = sprite.getInterpolatedU(maxUV);
            uvs[V_MIN] = sprite.getInterpolatedV(minUV);
            uvs[V_MAX] = sprite.getInterpolatedV(maxUV);

            for (EnumFacing face : EnumFacing.VALUES) {
                if (!render.pipeConnectionMatrix.isConnected(face) || !render.pipeConnectionBanned.isConnected(face)) {
                    Vec3 radius = Utils.vec3(0.25);
                    if (smaller) {
                        double smallerValue = Utils.getValue(radius, face.getAxis()) - 0.01f;
                        radius = Utils.withValue(radius, face.getAxis(), smallerValue);
                    }

                    List<MutableQuad> quadsIn = new ArrayList<>();
                    List<MutableQuad> quadsOut = new ArrayList<>();

                    Tuple3f center = Utils.vec3f(0.5f);
                    Tuple3f radiusf = Utils.convertFloat(radius);

                    BCModelHelper.appendQuads(quadsIn, BCModelHelper.createFace(face, center, radiusf, uvs));
                    BCModelHelper.appendQuads(quadsOut, BCModelHelper.createInverseFace(face, center, radiusf, uvs));
                    if (!BCModelHelper.shouldInvertForRender(face)) {
                        for (MutableQuad q : quadsIn) {
                            q.colourf(insideColourMult, insideColourMult, insideColourMult, 1);
                            BCModelHelper.appendBakeQuads(quads, q);
                        }
                        BCModelHelper.appendBakeQuads(quads, quadsOut);
                    } else {
                        BCModelHelper.appendBakeQuads(quads, quadsIn);
                        for (MutableQuad q : quadsOut) {
                            q.colourf(insideColourMult, insideColourMult, insideColourMult, 1);
                            BCModelHelper.appendBakeQuads(quads, q);
                        }
                    }
                }
            }
        }

        // All the connected bits
        for (EnumFacing connect : EnumFacing.VALUES) {
            if (render.pipeConnectionMatrix.isConnected(connect) && render.pipeConnectionBanned.isConnected(connect)) {
                float extension = render.customConnections[connect.ordinal()];
                TextureAtlasSprite sprite = spriteMap.get(connect);

                Vec3 actualCenter = Utils.convert(connect, 0.375f + extension / 2).add(Utils.VEC_HALF);

                Vec3 smallerFace = null;
                if (connect.getAxisDirection() == AxisDirection.POSITIVE) {
                    smallerFace = Utils.convert(connect, 4 / 16d - extension);
                } else {
                    smallerFace = Utils.convert(connect.getOpposite(), 4 / 16d - extension);
                }
                Vec3 actualSize = Utils.VEC_HALF.subtract(smallerFace);

                if (smaller) {
                    // Decrease the entire size
                    Vec3 allSmaller = actualSize.subtract(Utils.vec3(0.02));
                    // Increase the size of axis the connection is in.
                    actualSize = allSmaller.add(Utils.convert(Utils.convertPositive(connect), 0.02));
                }

                Vec3 pos = actualCenter.subtract(Utils.multiply(actualSize, 1 / 2d));

                EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
                cuboid.texture = sprite;
                cuboid.makeClient();

                double start = connect.getAxisDirection() == AxisDirection.POSITIVE ? 12 : 0;

                cuboid.textureStartX = connect.getAxis() == Axis.X ? start : 4;
                cuboid.textureStartY = connect.getAxis() == Axis.Y ? start : 4;
                cuboid.textureStartZ = connect.getAxis() == Axis.Z ? start : 4;

                cuboid.textureSizeX = connect.getAxis() == Axis.X ? 4 : 8;
                cuboid.textureSizeY = connect.getAxis() == Axis.Y ? 4 : 8;
                cuboid.textureSizeZ = connect.getAxis() == Axis.Z ? 4 : 8;

                cuboid.textures[connect.ordinal()] = null;
                cuboid.textures[connect.getOpposite().ordinal()] = null;

                cuboid.setSize(actualSize);
                cuboid.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);

                List<MutableQuad> quadsIn = new ArrayList<>();
                List<MutableQuad> quadsOut = new ArrayList<>();

                RenderResizableCuboid.bakeCube(quadsIn, cuboid, false, true);
                RenderResizableCuboid.bakeCube(quadsOut, cuboid, true, false);

                for (MutableQuad mutable : quadsIn) {
                    mutable.colourf(insideColourMult, insideColourMult, insideColourMult, 1);
                    BCModelHelper.appendBakeQuads(quads, mutable);
                }
                BCModelHelper.appendBakeQuads(quads, quadsOut);
            }
        }
    }

    // The main block model
    private static void renderCutoutPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe, List<BakedQuad> quads) {
        Map<EnumFacing, TextureAtlasSprite> spriteMap = Maps.newHashMap();
        for (EnumFacing face : EnumFacing.values()) {
            spriteMap.put(face, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(face)));
        }
        spriteMap.put(null, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(null)));
        renderPipe(render, quads, spriteMap, false);

        // Pluggables
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
            if (plug != null) {
                IPipePluggableStaticRenderer plugRender = plug.getRenderer();
                if (plugRender != null) {
                    List<BakedQuad> list = plugRender.bakeCutout(render, pluggable, pipe, plug, face);
                    if (list != null) {
                        quads.addAll(list);
                    }
                }
            }
        }

        // Wires
        PipeRendererWires.renderPipeWires(quads, render);
    }

    // Used basically for pipe colour
    private static void renderTranslucentPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe, List<BakedQuad> quads) {
        if (render.getGlassColor() >= 0 && render.getGlassColor() < 16) {
            Map<EnumFacing, TextureAtlasSprite> spriteMap = Maps.newHashMap();
            TextureAtlasSprite sprite = PipeIconProvider.TYPE.PipeStainedOverlay.getIcon();
            for (EnumFacing face : EnumFacing.values()) {
                spriteMap.put(face, sprite);
            }
            spriteMap.put(null, sprite);

            // Grab the first index to apply shading to
            int startIndex = quads.size();

            renderPipe(render, quads, spriteMap, true);

            int colour = ColorUtils.getRGBColor(render.getGlassColor());
            for (int i = startIndex; i < quads.size(); i++) {
                quads.get(i).getTintIndex();
                BakedQuad shapeQuad = quads.get(i);
                BakedQuad colouredQuad = new BakedQuad(shapeQuad.getVertexData(), colour, shapeQuad.getFace());
                quads.set(i, colouredQuad);
            }
        }

        // Pluggables
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
            if (plug != null) {
                IPipePluggableStaticRenderer plugRender = plug.getRenderer();
                if (plugRender instanceof IPipePluggableStaticRenderer.Translucent) {
                    Translucent baker = (Translucent) plugRender;
                    List<BakedQuad> list = baker.bakeTranslucent(render, pluggable, pipe, plug, face);
                    if (list != null) {
                        quads.addAll(list);
                    }
                }
            }
        }
    }
}
