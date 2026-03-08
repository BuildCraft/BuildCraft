package buildcraft.energy.client.render;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.client.model.MutableQuad;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

public class RenderDynamoMJ implements BlockEntityRenderer<TileDynamoMJ> {
    // public static final RenderDynamoMJ INSTANCE = new RenderDynamoMJ();

    public RenderDynamoMJ(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            TileDynamoMJ engine, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lightc, int combinedOverlay
    ) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("engine");

        profiler.push("compute");
        // vb.setTranslation(x, y, z);
        MutableQuad[] quads = BCEnergyModels.getMjDynamoQuads(engine, partialTicks);
        profiler.popPush("render");
        MutableQuad copy = new MutableQuad(0, null);
        // int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        VertexConsumer vb = bufferSource.getBuffer(RenderType.cutout());
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.maxLighti((byte) light_block, (byte) light_sky);
            copy.multShade();
            copy.render(poseStack.last(), vb);
        }
        // vb.setTranslation(0, 0, 0);

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }
}
