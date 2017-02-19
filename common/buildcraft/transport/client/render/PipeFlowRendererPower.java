package buildcraft.transport.client.render;

import net.minecraft.client.renderer.VertexBuffer;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.transport.pipe.flow.PipeFlowPower;

public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {

    }
}
