package buildcraft.transport.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.core.lib.client.model.BuildCraftBakedModel;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe.CoreState;

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

        ImmutableList<BakedQuad> quads;

        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            quads = renderCutoutPass(render, pluggable, pipe);
        } else if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            quads = renderTranslucentPass(render, pluggable, pipe);
        } else {
            quads = ImmutableList.of();
        }

        TextureAtlasSprite particle = pipe.getIconProvider().getIcon(pipe.getIconIndex(null));

        return new PipeBlockModel(quads, particle, DefaultVertexFormats.BLOCK);
    }

    // The main block model
    private static ImmutableList<BakedQuad> renderCutoutPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe) {
        return PipeModelCacheAll.getCutoutModel(pipe, render, pluggable);

        // Map<EnumFacing, TextureAtlasSprite> spriteMap = Maps.newHashMap();
        // for (EnumFacing face : EnumFacing.values()) {
        // spriteMap.put(face, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(face)));
        // }
        // spriteMap.put(null, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(null)));
        // quads.addAll(PipeModelCacheBase.getCutoutModel(render, spriteMap));
        //
        // // Pluggables
        // for (EnumFacing face : EnumFacing.VALUES) {
        // PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
        // if (plug != null) {
        // IPipePluggableStaticRenderer plugRender = plug.getRenderer();
        // if (plugRender != null) {
        // List<BakedQuad> list = plugRender.bakeCutout(render, pluggable, pipe, plug, face);
        // if (list != null) {
        // quads.addAll(list);
        // }
        // }
        // }
        // }
        //
        // // Facades
        // PipeRendererFacades.renderPipeFacades(quads, pluggable);
        //
        // // Wires
        // PipeRendererWires.renderPipeWires(quads, render);
    }

    // Used basically for pipe colour
    private static ImmutableList<BakedQuad> renderTranslucentPass(PipeRenderState render, PipePluggableState pluggable, Pipe<?> pipe) {
        return PipeModelCacheAll.getTranslucentModel(pipe, render, pluggable);

        // if (render.getGlassColor() >= 0 && render.getGlassColor() < 16) {
        // quads.addAll(PipeModelCacheBase.getTranslucentModel(render));
        // }
        //
        // // Pluggables
        // for (EnumFacing face : EnumFacing.VALUES) {
        // PipePluggable plug = pluggable.getPluggables()[face.ordinal()];
        // if (plug != null) {
        // IPipePluggableStaticRenderer plugRender = plug.getRenderer();
        // if (plugRender instanceof IPipePluggableStaticRenderer.Translucent) {
        // Translucent baker = (Translucent) plugRender;
        // List<BakedQuad> list = baker.bakeTranslucent(render, pluggable, pipe, plug, face);
        // if (list != null) {
        // quads.addAll(list);
        // }
        // }
        // }
        // }
    }
}
