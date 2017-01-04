package buildcraft.lib.client.render.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.engine.TileEngineBase_BC8;

public abstract class RenderEngine_BC8<T extends TileEngineBase_BC8> extends FastTESR<T> {
    // TODO: Cache the model!

    @Override
    public void renderTileEntityFast(T engine, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("engine");

        vb.setTranslation(x, y, z);
        MutableQuad[] quads = getEngineModel(engine, partialTicks);
        MutableQuad copy = new MutableQuad(0, null);
        int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            for (int i = 0; i < 4; i++) {
                MutableVertex v = copy.getVertex(i);
                if (v.light_block < light_block) v.light_block = (byte) light_block;
                v.light_sky = (byte) light_sky;
            }
            if (copy.isShade()) {
                copy.setShade(false);
                copy.multColourd(copy.getCalculatedDiffuse());
            }
            copy.render(vb);
        }
        vb.setTranslation(0, 0, 0);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    protected abstract MutableQuad[] getEngineModel(T engine, float partialTicks);
}
