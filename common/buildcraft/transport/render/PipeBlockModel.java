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

import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe.CoreState;
import buildcraft.transport.render.tile.PipeRendererWires;

public class PipeBlockModel extends BuildCraftBakedModel implements ISmartBlockModel {
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

        float min = CoreConstants.PIPE_MIN_POS;
        float max = CoreConstants.PIPE_MAX_POS;

        float minUV = min * 16;
        float maxUV = max * 16;

        // Center bit
        {
            TextureAtlasSprite sprite = pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(null));

            float[] uvs = new float[4];
            uvs[U_MIN] = sprite.getInterpolatedU(minUV);
            uvs[U_MAX] = sprite.getInterpolatedU(maxUV);
            uvs[V_MIN] = sprite.getInterpolatedV(minUV);
            uvs[V_MAX] = sprite.getInterpolatedV(maxUV);

            for (EnumFacing face : EnumFacing.VALUES) {
                if (!render.pipeConnectionMatrix.isConnected(face) || !render.pipeConnectionBanned.isConnected(face)) {
                    bakeDoubleFace(quads, face, new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.25f, 0.25f, 0.25f), uvs);
                }
            }
        }

        // All the connected bits
        for (EnumFacing connect : EnumFacing.VALUES) {
            if (render.pipeConnectionMatrix.isConnected(connect) && render.pipeConnectionBanned.isConnected(connect)) {
                float extension = render.customConnections[connect.ordinal()];
                TextureAtlasSprite sprite = pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(connect));

                Vec3 actualCenter = Utils.convert(connect, 0.375f + extension / 2).addVector(0.5, 0.5, 0.5);

                Vec3 smallerFace = null;
                if (connect.getAxisDirection() == AxisDirection.POSITIVE) {
                    smallerFace = Utils.convert(connect, 4 / 16d - extension);
                } else {
                    smallerFace = Utils.convert(connect.getOpposite(), 4 / 16d - extension);
                }
                Vec3 actualSize = Utils.VEC_HALF.subtract(smallerFace);

                Vec3 pos = actualCenter.subtract(Utils.multiply(actualSize, 1 / 2d));

                EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
                cuboid.texture = sprite;
                cuboid.makeClient();

                // The extra 0.001 is to stop a bug where the next texture along is used for a pixel of the cuboid

                double start = connect.getAxisDirection() == AxisDirection.POSITIVE ? 12.001 : 0.001;

                cuboid.textureStartX = connect.getAxis() == Axis.X ? start : 4.001;
                cuboid.textureStartY = connect.getAxis() == Axis.Y ? start : 4.001;
                cuboid.textureStartZ = connect.getAxis() == Axis.Z ? start : 4.001;

                cuboid.textureSizeX = connect.getAxis() == Axis.X ? 3.998 : 7.998;
                cuboid.textureSizeY = connect.getAxis() == Axis.Y ? 3.998 : 7.998;
                cuboid.textureSizeZ = connect.getAxis() == Axis.Z ? 3.998 : 7.998;

                cuboid.textures[connect.ordinal()] = null;
                cuboid.textures[connect.getOpposite().ordinal()] = null;

                cuboid.setSize(actualSize);
                cuboid.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);

                RenderResizableCuboid.INSTANCE.renderCubeStatic(quads, cuboid);
            }
        }

        // Wires
        PipeRendererWires.renderPipeWires(quads, render);

        // Pluggables
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
            if (plug != null) {
                IPipePluggableStaticRenderer plugRender = plug.getRenderer();
                if (plugRender != null) {
                    List<BakedQuad> list = plugRender.renderStaticPluggable(render, pluggable, pipe, plug, face);
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
