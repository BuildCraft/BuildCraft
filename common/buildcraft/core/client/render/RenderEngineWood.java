package buildcraft.core.client.render;

import buildcraft.core.BCCoreModels;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

public class RenderEngineWood extends RenderEngine_BC8<TileEngineRedstone_BC8> {
    @Override
    protected MutableQuad[] getEngineModel(TileEngineRedstone_BC8 engine, float partialTicks) {
        return BCCoreModels.getRedstoneEngineQuads(engine, partialTicks);
    }
}
