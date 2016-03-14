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
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BuildCraftBakedModel;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe.CoreState;
import buildcraft.transport.render.tile.PipeModelCacheBase;
import buildcraft.transport.render.tile.PipeRendererFacades;
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

        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            renderCutoutPass(render, pluggable, pipe, quads);
        } else if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            renderTranslucentPass(render, pluggable, pipe, quads);
        }

        TextureAtlasSprite particle = pipe.getIconProvider().getIcon(pipe.getIconIndex(null));

        return new PipeBlockModel(ImmutableList.copyOf(quads), particle, DefaultVertexFormats.BLOCK);
    }

    // The main block model
    private static void renderCutoutPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe, List<BakedQuad> quads) {
        Map<EnumFacing, TextureAtlasSprite> spriteMap = Maps.newHashMap();
        for (EnumFacing face : EnumFacing.values()) {
            spriteMap.put(face, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(face)));
        }
        spriteMap.put(null, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(null)));
        quads.addAll(PipeModelCacheBase.getCutoutModel(render, spriteMap));

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

        // Facades
        PipeRendererFacades.renderPipeFacades(quads, pluggable);

        // Wires
        PipeRendererWires.renderPipeWires(quads, render);
    }

    // Used basically for pipe colour
    private static void renderTranslucentPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe, List<BakedQuad> quads) {
        if (render.getGlassColor() >= 0 && render.getGlassColor() < 16) {
            quads.addAll(PipeModelCacheBase.getTranslucentModel(render));
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
