package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;

public abstract class RenderEngine_BC8<T extends TileEngineBase_BC8> extends FastTESR<T> {

    @Override
    public void renderTileEntityFast(T engine, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        MutableQuad[] quads = getEngineModel(engine, partialTicks);
        for (MutableQuad q : quads) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }

    protected abstract MutableQuad[] getEngineModel(T engine, float partialTicks);
}
