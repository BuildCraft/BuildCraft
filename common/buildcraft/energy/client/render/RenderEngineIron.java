package buildcraft.energy.client.render;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

public class RenderEngineIron extends RenderEngine_BC8<TileEngineIron_BC8> {
    public static final RenderEngineIron INSTANCE = new RenderEngineIron();

    @Override
    protected MutableQuad[] getEngineModel(TileEngineIron_BC8 engine, float partialTicks) {
        return BCEnergyModels.getIronEngineQuads(engine, partialTicks);
    }
}
