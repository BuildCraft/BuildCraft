package buildcraft.energy.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.profiler.Profiler;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileDynamoMJ;

public class RenderDynamoMJ extends FastTESR<TileDynamoMJ> {
    public static final RenderDynamoMJ INSTANCE = new RenderDynamoMJ();

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void renderTileEntityFast(
        TileDynamoMJ engine, double x, double y, double z, float partialTicks, int destroyStage, float partial,
        BufferBuilder vb
    ) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("engine");

        profiler.push("compute");
        // TODO(R.Chen): setTranslation removed — use MatrixStack instead: vb.setTranslation(x, y, z);
        MutableQuad[] quads = BCEnergyModels.getMjDynamoQuads(engine, partialTicks);
        profiler.swap("render");
        MutableQuad copy = new MutableQuad(0, null);
        int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.maxLighti(light_block, light_sky);
            copy.multShade();
            copy.render(vb);
        }
        // TODO(R.Chen): setTranslation removed — use MatrixStack instead: vb.setTranslation(0, 0, 0);

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }

}
