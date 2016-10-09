package buildcraft.transport.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.BCLibProxy;
import buildcraft.transport.api_move.IPluggableDynamicRenderer;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.tile.TilePipeHolder;

public class RenderPipeHolder extends FastTESR<TilePipeHolder> {

    @Override
    public void renderTileEntityFast(TilePipeHolder pipe, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {

        pipe = BCLibProxy.getProxy().getServerTile(pipe);

        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("pipe");

        Minecraft.getMinecraft().mcProfiler.startSection("wire");
        renderWire(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("pluggable");
        renderPluggables(pipe, x, y, z, vb);

        Minecraft.getMinecraft().mcProfiler.endStartSection("contents");
        renderContents(pipe, x, y, z, partialTicks, vb);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderWire(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        // TODO!
    }

    private static void renderPluggables(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == null) {
                continue;
            }
            IPluggableDynamicRenderer dynRenderer = plug.getDynamicRenderer();
            if (dynRenderer == null) {
                continue;
            }
            dynRenderer.render(x, y, z, vb);
        }
    }

    private void renderContents(TilePipeHolder pipe, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        Pipe p = pipe.getPipe();
        if (p != null && p.flow != null) {
            if (p.flow instanceof PipeFlowItems) {
                RendererPipeFlowItems.INSTANCE.render((PipeFlowItems) p.flow, x, y, z, partialTicks, vb);
            }
        }
    }
}
