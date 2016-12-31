package buildcraft.transport.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;
import buildcraft.api.transport.neptune.PipePluggable;

import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {
    @Override
    public void renderTileEntityFast(TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        PipeWireRenderer.renderWires(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");
        renderContents(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) {
                continue;
            }
            IPluggableDynamicRenderer dynRenderer = plug.getDynamicRenderer();
            if (dynRenderer == null) {
                continue;
            }
            dynRenderer.render(x, y, z, partialTicks, vb);
        }
    }

    private void renderContents(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        Pipe p = pipe.getPipe();
        if (p != null && p.flow != null) {
            if (p.flow instanceof PipeFlowItems) {
                PipeFlowRendererItems.INSTANCE.render((PipeFlowItems) p.flow, x, y, z, partialTicks, vb);
            } else if (p.flow instanceof PipeFlowFluids) {
                PipeFlowRendererFluids.INSTANCE.render((PipeFlowFluids) p.flow, x, y, z, partialTicks, vb);
            }
        }
    }
}
