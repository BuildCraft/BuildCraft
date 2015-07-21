package buildcraft.transport.render;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe.CoreState;
import buildcraft.transport.block.BlockGenericPipe;

public class PipeRenderModel extends BuildCraftBakedModel implements ISmartBlockModel {
    public PipeRenderModel() {
        super(null, null, null);
    }

    protected PipeRenderModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        super(quads, particle, format);
    }

    @Override
    public ISmartBlockModel handleBlockState(IBlockState state) {
        return handle((IExtendedBlockState) state);
    }

    public static ISmartBlockModel handle(IExtendedBlockState state) {
        CoreState core = BlockGenericPipe.PIPE_CORE_STATE.getUnlistedValue(state);
        PipeRenderState render = BlockGenericPipe.PIPE_RENDER_STATE.getUnlistedValue(state);
        PipePluggableState pluggable = BlockGenericPipe.PIPE_PLUGGABLE_STATE.getUnlistedValue(state);

        return new PipeRenderModel();
    }
}
