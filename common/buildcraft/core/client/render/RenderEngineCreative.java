package buildcraft.core.client.render;

import buildcraft.core.BCCoreModels;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

public class RenderEngineCreative extends RenderEngine_BC8<TileEngineCreative> {
    public static final RenderEngineCreative INSTANCE = new RenderEngineCreative();

    @Override
    protected MutableQuad[] getEngineModel(TileEngineCreative engine, float partialTicks) {
        return BCCoreModels.getCreativeEngineQuads(engine, partialTicks);
    }
}
