package buildcraft.energy.client.render;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

public class RenderEngineStone extends RenderEngine_BC8<TileEngineStone_BC8> {
    public static final RenderEngineStone INSTANCE = new RenderEngineStone();

    @Override
    protected MutableQuad[] getEngineModel(TileEngineStone_BC8 engine, float partialTicks) {
        return BCEnergyModels.getStoneEngineQuads(engine, partialTicks);
    }
}
