package buildcraft.transport.render;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe.CoreState;
import buildcraft.transport.block.BlockGenericPipe;

public class PipeBlockModel extends BuildCraftBakedModel implements ISmartBlockModel {
    // private static final Map<Integer, Pipe<?>> pipes = Maps.newHashMap();

    public PipeBlockModel() {
        super(ImmutableList.<BakedQuad> of(), null, null);
    }

    protected PipeBlockModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
    }

    @Override
    public ISmartBlockModel handleBlockState(IBlockState state) {
        return handle((IExtendedBlockState) state);
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
            return defaultModel();// Thats not good. Just return a cobblestone structure pipe center model
        }

        // if (!pipes.containsKey(core.pipeId)) {
        // pipes.put(core.pipeId, BlockGenericPipe.createPipe(Item.getItemById(core.pipeId)));
        // }
        // Pipe<?> pipe = pipes.get(core.pipeId);

        // if (pipe == null) {
        // return defaultModel();
        // }
        List<BakedQuad> quads = Lists.newArrayList();

        float min = CoreConstants.PIPE_MIN_POS;
        float max = CoreConstants.PIPE_MAX_POS;

        float minUV = min * 16;
        float maxUV = max * 16;

        // Center bit
        {
            TextureAtlasSprite sprite = pipe.getIconProvider().getIcon(pipe.getIconIndex(null));

            float[] uvs = new float[4];
            uvs[U_MIN] = sprite.getInterpolatedU(minUV);
            uvs[U_MAX] = sprite.getInterpolatedU(maxUV);
            uvs[V_MIN] = sprite.getInterpolatedV(minUV);
            uvs[V_MAX] = sprite.getInterpolatedV(maxUV);

            for (EnumFacing face : EnumFacing.VALUES) {
                if (!render.pipeConnectionMatrix.isConnected(face)) {
                    bakeDoubleFace(quads, face, new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.25f, 0.25f, 0.25f), uvs);
                }
            }
        }

        // All the connected bits
        for (EnumFacing connect : EnumFacing.VALUES) {
            if (render.pipeConnectionMatrix.isConnected(connect)) {
                TextureAtlasSprite sprite = pipe.getIconProvider().getIcon(pipe.getIconIndex(connect));
                Vec3 actualCenter = Utils.convert(connect, 0.375f).addVector(0.5, 0.5, 0.5);
                Vector3f center = Utils.convertFloat(actualCenter);

                for (EnumFacing face : EnumFacing.VALUES) {
                    if (face.getAxis() == connect.getAxis()) {
                        continue;
                    }

                    Vec3 smallerFace = null;
                    if (connect.getAxisDirection() == AxisDirection.POSITIVE) {
                        smallerFace = Utils.convert(connect, 2 / 16d);
                    } else {
                        smallerFace = Utils.convert(connect.getOpposite(), 2 / 16d);
                    }
                    Vec3 actualRadius = new Vec3(0.25, 0.25, 0.25).subtract(smallerFace);
                    Vector3f radius = Utils.convertFloat(actualRadius);

                    double umin = 0, umax = 0, vmin = 0, vmax = 0;

                    if (connect == EnumFacing.UP) {
                        umin = 4;
                        umax = 12;
                        vmin = 0;
                        vmax = 4;
                    } else if (connect == EnumFacing.DOWN) {
                        umin = 4;
                        umax = 12;
                        vmin = 12;
                        vmax = 16;
                    } else {
                        boolean vertical = false;
                        boolean positive = false;

                        if (connect == EnumFacing.NORTH) {
                            vertical = face.getAxis() == Axis.Y;
                            positive = face.getAxis() == Axis.X;
                        } else if (connect == EnumFacing.SOUTH) {
                            vertical = face.getAxis() == Axis.Y;
                            positive = face.getAxis() != Axis.X;
                        } else if (connect == EnumFacing.EAST) {
                            positive = false;
                        } else if (connect == EnumFacing.WEST) {
                            positive = true;
                        }

                        if (vertical) {
                            if (positive) {
                                umin = 4;
                                umax = 12;
                                vmin = 0;
                                vmax = 4;
                            } else {
                                umin = 4;
                                umax = 12;
                                vmin = 12;
                                vmax = 16;
                            }
                        } else {
                            if (positive) {
                                umin = 0;
                                umax = 4;
                                vmin = 4;
                                vmax = 12;
                            } else {
                                umin = 12;
                                umax = 16;
                                vmin = 4;
                                vmax = 12;
                            }
                        }
                    }

                    float[] uvs = new float[4];
                    uvs[U_MIN] = sprite.getInterpolatedU(umin);
                    uvs[U_MAX] = sprite.getInterpolatedU(umax);
                    uvs[V_MIN] = sprite.getInterpolatedV(vmin);
                    uvs[V_MAX] = sprite.getInterpolatedV(vmax);

                    bakeDoubleFace(quads, face, center, radius, uvs);
                }
            }
        }

        // Wires

        // Pluggables
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
            if (plug != null) {
                IPipePluggableRenderer plugRender = plug.getRenderer();
                if (plugRender != null) {
                    List<BakedQuad> list = plugRender.renderPluggable(pipe, plug, face);
                    if (list != null) {
                        quads.addAll(list);
                    }
                }
            }
        }

        TextureAtlasSprite particle = pipe.getIconProvider().getIcon(pipe.getIconIndex(null));

        return new PipeBlockModel(ImmutableList.copyOf(quads), particle, DefaultVertexFormats.BLOCK);
    }
}
